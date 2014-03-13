package de.fusionfactory.index_vivus.persistence

import scala.slick.session.{Session, Database}
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import de.fusionfactory.index_vivus.configuration.SettingsProvider
import de.fusionfactory.index_vivus.models.scalaimpl.{GermanTokens, AbbreviationOccurrences, Abbreviations, DictionaryEntries}
import scala.slick.lifted.Query
import SlickTools.{database => db}

import org.apache.log4j.Logger
import java.sql.SQLException
import com.mchange.v2.c3p0.ComboPooledDataSource

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object SlickTools {

  lazy val logger = Logger.getLogger(SlickTools.getClass)

  def transactionForSession[T](session: Session)(work: Session => T): T = session.withTransaction(work(session))

  def inTransaction[T](s: Option[Session])(work: Session => T): T = {
    if (s.isEmpty)
      db.withSession(s => transactionForSession(s)(work))
    else
      transactionForSession(s.get)(work)
  }

  lazy val database =  {
    val cpds = new ComboPooledDataSource()
    cpds.setDriverClass("org.h2.Driver")
    cpds.setJdbcUrl(SettingsProvider.getInstance.getDatabaseUrl)
    cpds.setMinPoolSize(4)
    cpds.setMaxPoolSize(32)
    cpds.setAcquireIncrement(4)
    cpds.setMaxStatements(1024)
    cpds.setMaxStatementsPerConnection(32)

    Database.forDataSource(cpds)
  }

  lazy val slickTables: Set[Table[_]] = Set(DictionaryEntries, Abbreviations, AbbreviationOccurrences, GermanTokens)

  def createMissingTables() = {
    def tableExists(table: Table[_])(implicit t: Session) = {
      try {
        Query(table).exists.run
        true
      } catch {
        case sqlEx: SQLException => {
          val tableNotFoundPattern = """^(?s:.*)Table .+ not found;(?s:.*)""".r
          val errorMsg = sqlEx.getMessage
          logger debug s"$errorMsg; machtes?: ${tableNotFoundPattern.findFirstMatchIn(errorMsg).isDefined}"
          errorMsg match {
            case tableNotFoundPattern(_*) => false
            case _ => throw sqlEx
          }
        }
      }
    }

    val work: Session => Unit = implicit t => {
      val missingTables = slickTables.filterNot(tableExists(_))
      if (!missingTables.isEmpty) {
        val ddlStmts = missingTables.map(_.ddl).reduce(_ ++ _)
        logger debug s"Executing DDL statements:\n ${ddlStmts.createStatements.mkString("\n")}\n"
        ddlStmts.create
      }
    }

    database withTransaction work
  }

}

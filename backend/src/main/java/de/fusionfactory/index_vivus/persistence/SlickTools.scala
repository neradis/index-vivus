package de.fusionfactory.index_vivus.persistence

import scala.slick.session.{Session, Database}
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import de.fusionfactory.index_vivus.configuration.SettingsProvider
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntries
import scala.slick.lifted.Query

import org.apache.log4j.Logger
import java.sql.SQLException

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object SlickTools {

  lazy val logger = Logger.getLogger(SlickTools.getClass)

  def transactionForSession[T](session: Session)(work: Session => T): T = session.withTransaction(work(session))

  lazy val database = Database.forURL(SettingsProvider.getInstance.getDatabaseUrl, driver = "org.h2.Driver")

  lazy val slickTables = Set(DictionaryEntries)

  def createMissingTables() = {
    for (table <- slickTables) yield {
      val work: Session => Unit = implicit t => {
        try {
          Query(table).exists.run
        } catch {
          case sqlEx: SQLException => {
            val tableNotFoundPattern = """^(?s:.*)Table .+ not found;(?s:.*)""".r
            val errorMsg = sqlEx.getMessage
            logger info s"$errorMsg; machtes: ${tableNotFoundPattern.findFirstMatchIn(errorMsg).isDefined}"
            errorMsg match {
              case tableNotFoundPattern(_*) => {
                logger.info(s"creating table ${DictionaryEntries.tableName} in " +
                  SettingsProvider.getInstance.getDatabaseUrl)
                table.ddl.create
              }
              case _ => throw sqlEx
            }
          }
        }
      }
      database withTransaction work
    }
  }
}

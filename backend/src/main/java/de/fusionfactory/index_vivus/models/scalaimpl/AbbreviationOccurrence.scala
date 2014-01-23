package de.fusionfactory.index_vivus.models.scalaimpl

import com.google.common.base.Optional
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db, _}
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs, Abbreviations => Abbr,
  AbbreviationOccurrences => AbbOccs}
import java.util.{List => JList}
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import scala.slick.session.Session
import scala.collection.convert.wrapAll._
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
object AbbreviationOccurrence {

  def create(entryId: Int, abbrId: Int): Int = db.withTransaction(implicit t => AbbOccs.insert((entryId, abbrId)))

  def create(entryId: Int, abbrId: Int, s: Session): Int =
    transactionForSession(s)(implicit t => AbbOccs.insert((entryId, abbrId)))

  def create(entryId: Int, abbrIds: JList[Integer]): Optional[Integer] = db.withTransaction(implicit t => {
    AbbOccs.insertAll(abbrIds.map( aid => (entryId, aid.toInt) ) : _*)
  })

  def create(entryId: Int, abbrIds: JList[Integer], s: Session): Optional[Integer] =
    transactionForSession(s)(implicit t => {
    AbbOccs.insertAll(abbrIds.map( aid => (entryId, aid.toInt) ) : _*)
  })

  def exists(entryId: Int, abbrId: Int): Boolean =
    db.withTransaction(implicit t => AbbOccs.byIdsQuery(entryId,abbrId).exists.run)

  def exists(entryId: Int, abbrId: Int, s: Session): Boolean =
    transactionForSession(s)(implicit t => AbbOccs.byIdsQuery(entryId,abbrId).exists.run)
}

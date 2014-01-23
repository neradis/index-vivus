package de.fusionfactory.index_vivus.models.scalaimpl

import com.google.common.base.Optional
import de.fusionfactory.index_vivus.models.{IDictionaryEntry, WordType}
import scala.beans.BeanProperty
import java.util.{List => JList}
import scala.slick.session.Session
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs}
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db, _}
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import scala.collection.convert.wrapAll._
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import DictionaryEntryBean.pos2Byte


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

object DictionaryEntry {

  def apply(prevId: Option[Int] = None, nextId: Option[Int] = None, keywordGroupIndex: Byte,keyword: String,
            description: String, htmlDescription: Option[String]  = None, pos: WordType = WordType.UNKNOWN):DictionaryEntry = {
    apply(None, prevId, nextId, keywordGroupIndex, keyword, description, htmlDescription, pos2Byte(pos))
  }

  def create(prevId: Optional[Integer], nextId: Optional[Integer], keywordGroupIndex: Byte,keyword: String,
                    description: String, htmlDescription: Optional[String], pos: WordType): DictionaryEntry = {
    apply(prevId, nextId, keywordGroupIndex, keyword, description, htmlDescription, pos)
  }

  def fetchById(id: Int): Optional[DictionaryEntry] = db.withSession( implicit s => DEs.byIdQuery(id).firstOption )


  def fetchById(id: Int, s: Session): Optional[DictionaryEntry] =
    transactionForSession(s)( implicit s => DictionaryEntries.byIdQuery(id).firstOption )

  def fetchByKeyword(keyword: String): JList[DictionaryEntry] = db.withSession(implicit s =>
    DEs.byKeywordQuery(keyword).list)

  def fetchByKeyword(keyword: String, s: Session): JList[DictionaryEntry] =
    transactionForSession(s)( implicit s => DEs.byKeywordQuery(keyword).list )

  def fetchByKeywordAndGroupId(keyword: String, groupId: Byte): Optional[DictionaryEntry] =
    db.withSession(implicit s => DEs.byKeywordAndKWGIndexQuery(keyword, groupId).firstOption)

  def fetchByKeywordAndGroupId(keyword: String, groupId: Byte, s: Session): Optional[DictionaryEntry] =
    transactionForSession(s)(implicit s => DEs.byKeywordAndKWGIndexQuery(keyword, groupId).firstOption)

  def fetchAll(s: Session): JList[DictionaryEntry] = transactionForSession(s)( implicit s => Query(DEs).list )
}


case class DictionaryEntry /*protected[scalaimpl]*/ (id: Option[Int],
                                                 var prevId: Option[Int],
                                                 var nextId: Option[Int],
                                                 @BeanProperty var keywordGroupIndex: Byte,
                                                 @BeanProperty var keyword: String,
                                                 @BeanProperty var description: String,
                                                 var htmlDescription: Option[String],
                                                 var posIdx: Option[Byte])
  extends DictionaryEntryBean with  IDictionaryEntry {

  def fetchAbbreviations(s: Option[Session] = None): List[Abbreviation] = {
    val work: Session => List[Abbreviation] = { implicit s =>
      DEs.joinOccuringAbbreviationsQuery.list()
    }
    inTransaction(s)(work)
  }

  def contentsEqual(other: DictionaryEntry) = {
    def equalize(de: DictionaryEntry) = de.copy(id = None, prevId=None, nextId=None, keywordGroupIndex=0)
    equalize(this) == equalize(other)
  }
}
package de.fusionfactory.index_vivus.models.scalaimpl

import de.fusionfactory.index_vivus.models.{ICrudOpsProvider, ICrudOps, WordType}
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import scala.slick.session.Session
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}


import java.util.{List => JList}
import scala.collection.convert.wrapAll._
import com.google.common.base.Optional
import de.fusionfactory.index_vivus.persistence.ORMError

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

object DictionaryEntryBean {

  lazy val posIdxRange = 0 until WordType.values.length

  def pos2Byte(wt: WordType): Option[Byte] = wt match {
    case WordType.UNKNOWN => None
    case wt : WordType => Some(WordType.values().indexOf(wt).toByte)
  }

  def byte2Pos(idx: Option[Byte]): WordType = idx match {
    case Some(b: Byte) if posIdxRange contains b => WordType.values.apply(b)
    case None => WordType.UNKNOWN
    case _ => throw new IllegalStateException(s"index $idx out of bounds for ${classOf[WordType].getSimpleName}")
  }
}


trait DictionaryEntryBean extends ICrudOpsProvider[DictionaryEntry,DictionaryEntryCrudOps] { this: DictionaryEntry =>

  def getId: Int = id.get

  def getIdOptional: Optional[Integer] = id

  def getPrevId: Optional[Integer] = prevId

  def setPrevId(pid: Optional[Integer]): Unit = prevId = pid

  def getNextId: Optional[Integer] = nextId

  def setNextId(nid: Optional[Integer]): Unit = nextId = nid

  def getHtmlDescription: Optional[String] = htmlDescription

  def setHtmlDescription(hd: Optional[String]): Unit = htmlDescription = hd

  def getOccurringAbbreviations: JList[Abbreviation] = fetchAbbreviations()

  def getOccurringAbbreviations(s: Session): JList[Abbreviation] = fetchAbbreviations(Some(s))

  def crud = new DictionaryEntryCrudOps(this)

  def crud(s: Session) = new DictionaryEntryCrudOps(this, Some(s))
}

class DictionaryEntryCrudOps(de: DictionaryEntry, transaction: Option[Session] = None)
  extends ICrudOps[DictionaryEntry] {

  private def opTransaction[T](work: Session => T): T = transaction match {
    case None => db.withTransaction(work)
    case Some(tr: Session) => tr.withTransaction(work(tr))
  }

  def insertAsNew() = opTransaction( implicit t => {
      val id = DictionaryEntries.forInsert.returning(DictionaryEntries.id).insert(de)
      de.copy(id = Some(id) )
  })

  def update(): Int = opTransaction( implicit t => {
    if(de.id.isEmpty) throw new ORMError(s"cannot update $de with undefined id")
    DictionaryEntries.byIdQuery(de.id.get) update de
  })

  def delete(): Int = opTransaction( implicit t => {
    if(de.id.isEmpty) throw new ORMError(s"cannot delete $de with undefined id")
    DictionaryEntries.byIdQuery(de.id.get) delete 
  })

  def duplicateList(): JList[DictionaryEntry] = {
    val sameKeywordEntries = opTransaction( implicit t => {
      DictionaryEntries.byKeywordQuery(de.keyword).list
    })
    sameKeywordEntries filter de.contentsEqual
  }
}

package de.fusionfactory.index_vivus.models.scalaimpl

import de.fusionfactory.index_vivus.models._
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import scala.slick.driver.H2Driver.simple._
import scala.slick.session.Session
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}
import de.fusionfactory.index_vivus.services.scalaimpl._
import DictionaryEntry._

import java.util.{List => JList}
import scala.collection.convert.wrapAll._
import com.google.common.base.Optional
import de.fusionfactory.index_vivus.persistence.ORMError
import de.fusionfactory.index_vivus.services.Language
import scala.Some
import com.google.common.collect.ImmutableList

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
trait DictionaryEntryBean extends ICrudOpsProvider[DictionaryEntry,DictionaryEntryCrudOps] with IDictionaryEntry { this: DictionaryEntry =>

  def getId: Int = id.get

  def getIdOptional: Optional[Integer] = id

  def getLanguage: Language = sourceLanguage

  def getWordType: WordType = posIdx

  def setWordType(wordType: WordType): Unit = posIdx = wordType

  def getPreviousEntryId: Optional[Integer] = prevId

  def getPreviousEntry: Optional[_ <: IDictionaryEntry] = prevId.map( fetchById(_).get() )

  def setPreviousEntryId(pid: Optional[Integer]): Unit = prevId = pid

  def getNextEntryId: Optional[Integer] = nextId

  def getNextEntry: Optional[_ <: IDictionaryEntry] = nextId.map( fetchById(_).get() )

  def setNextEntryId(nid: Optional[Integer]): Unit = nextId = nid

  def getHtmlDescription: Optional[String] = htmlDescription

  def setHtmlDescription(hd: Optional[String]): Unit = htmlDescription = hd

  def getRelated: JList[_ <: IDictionaryEntry] = ImmutableList.of() //TODO: implement

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

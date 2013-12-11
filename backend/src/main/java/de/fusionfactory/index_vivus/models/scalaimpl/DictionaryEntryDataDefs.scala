package de.fusionfactory.index_vivus.models.scalaimpl

import circumflex.orm._
import de.fusionfactory.index_vivus.models.{scalaimpl, WordType, IDictionaryEntry}
import java.util
import com.google.common.base.Optional
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import de.fusionfactory.index_vivus.persistence.ORMError

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
class DictionaryEntry extends Record[Int, DictionaryEntry] with SequenceGenerator[Int, DictionaryEntry]
  with JDictionaryEntry {

  def PRIMARY_KEY = id
  def relation = DictionaryEntry

  val id = "id".INTEGER.NOT_NULL.AUTO_INCREMENT
  lazy val prevEntry = "prev_id".INTEGER.REFERENCES(DictionaryEntry).ON_DELETE(SET_NULL).ON_UPDATE(NO_ACTION)
  lazy val nextEntry = "next_id".INTEGER.REFERENCES(DictionaryEntry).ON_DELETE(SET_NULL).ON_UPDATE(NO_ACTION)
  val keywordGroupIndex = "keyword_group_id".INTEGER.NOT_NULL
  
  val keyword = "keyword".VARCHAR(512).NOT_NULL
  val description = "description".TEXT
  val htmlDescription ="html_description".TEXT
  
  val wordType = "word_type".INTEGER

  def this(keyword: String, description: String) {
    this()
    this.keyword := keyword
    this.description := description
  }

  override def toString(): String = super.toString() +
    s"${(keyword.getOrElse("[undefined]"), description.getOrElse("[undefined]"))}"
}

object DictionaryEntry extends DictionaryEntry with Table[Int, DictionaryEntry] with JDictionaryEntryObject {
  val NONE_DESCRIPTION = "[undefined]"

  val uniqId = UNIQUE(id)
  /*val wordTypeRange = CONSTRAINT("word_type_range").
                      CHECK(s"word_type  >= 0 AND word_type <= ${WordType.values().length -1}")*/

  def findAll = DictionaryEntry.criteria.list

  def findById(id: Int) = DictionaryEntry.criteria.add(DictionaryEntry.id EQ id).unique

  def findByKeyword(keyword: String) = DictionaryEntry.criteria.add(DictionaryEntry.keyword EQ keyword).list()

  def lastIdValue = (this AS "de").map( de => SELECT(MAX(de.id)).FROM(de)).unique
}

trait JDictionaryEntry extends IDictionaryEntry { this : DictionaryEntry =>
  import scalaimpl.DictionaryEntry._
  import DictionaryEntry.NONE_DESCRIPTION
  private val ABSENT_DICTIONARY_ENTRY = Optional.absent()
  
  def getId: Int = id.get match {
    case Some(id) => id
    case None => throw new UninitializedFieldError()
  }

  def getKeyword: String = keyword.getOrElse(NONE_DESCRIPTION)

  def setKeyword(keyword: String): Unit = this.keyword := keyword

  def getDescription: String = description.getOrElse(NONE_DESCRIPTION)

  def setDescription(description: String): Unit = this.description := description

  def getWordType: WordType = ???

  def setWordType(wordType: WordType): Unit = ???

  def getPreviousEntry: Optional[IDictionaryEntry] = prevEntry.get

  def setPreviousEntry(entry: Optional[_ <: IDictionaryEntry]): Unit = entry match {
    case ABSENT_DICTIONARY_ENTRY => prevEntry.set(None)
    case opt => withCorrectImplClass(opt.get, prevEntry.set) 
  }

  def getPreviousEntryId: Optional[Integer] = prevEntry.get.map(_.id.get) match {
    case Some(opt @ Some(i)) => opt
    case _ => None
  }

  def setPreviousEntryId(id: Optional[Integer]): Unit = optional2Option(id) match {
    case Some(id)  => prevEntry.set(findById(id))
    case None => prevEntry.set(None)
  } 
    
  def getNextEntry: Optional[IDictionaryEntry] = nextEntry.get

  def setNextEntry(entry: Optional[_ <: IDictionaryEntry]): Unit = entry match {
    case ABSENT_DICTIONARY_ENTRY => nextEntry.set(None)
    case opt => withCorrectImplClass(opt.get, nextEntry.set)
  }

  def getNextEntryId: Optional[Integer] =  nextEntry.get.map(_.id.get) match {
    case Some(opt @ Some(i)) => opt
    case _ => None
  }

  def setNextEntryId(id: Optional[Integer]): Unit = optional2Option(id) match {
    case Some(id)  => nextEntry.set(findById(id))
    case None => nextEntry.set(None)
  }

  def getKeywordGroupIndex: Int = keywordGroupIndex.get match {
    case Some(i) => i
    case None => throw new UninitializedFieldError()
  }

  def setKeywordGroupIndex(keywordGroupIndex: Int): Unit = this.keywordGroupIndex := keywordGroupIndex

  def getRelated: util.List[IDictionaryEntry] = ???
}

trait JDictionaryEntryObject { 
  import scalaimpl.DictionaryEntry._
  import scala.collection.JavaConverters._
  import java.util.{List => JList}

  def findByIdJava(id: Int) : Optional[DictionaryEntry] = findById(id)

  def findByKeywordJava(keyword: String) : JList[DictionaryEntry] = findByKeyword(keyword).asJava

  def insertJava(entry: IDictionaryEntry) = withCorrectImplClass(entry, _.INSERT())

  def updateJava(entry: IDictionaryEntry) = withCorrectImplClass(entry, _.UPDATE())

  def lastIdValueJava(): Optional[Integer] = lastIdValue
  
  def withCorrectImplClass[T](e: IDictionaryEntry, action : DictionaryEntry => T) = e match {
    case e : DictionaryEntry => action(e)
    case x => throw new ORMError(s"Unable to perform ORM action with concrete class ${x.getClass.getCanonicalName}")
  }
}

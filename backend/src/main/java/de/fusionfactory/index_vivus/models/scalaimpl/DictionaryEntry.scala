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
import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.services.scalaimpl._


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

object DictionaryEntry {

  def apply(lang: Language, prevId: Option[Int] = None, nextId: Option[Int] = None, keywordGroupIndex: Byte, keyword: String,
            description: String, htmlDescription: Option[String] = None, pos: WordType = WordType.UNKNOWN): DictionaryEntry = {
    apply(None, lang, prevId, nextId, keywordGroupIndex, keyword, description, htmlDescription, pos)
  }

  def create(lang: Language, prevId: Optional[Integer], nextId: Optional[Integer], keywordGroupIndex: Byte, keyword: String,
             description: String, htmlDescription: Optional[String], pos: WordType): DictionaryEntry = {
    apply(lang, prevId, nextId, keywordGroupIndex, keyword, description, htmlDescription, pos)
  }

  def fetchById(id: Int): Optional[DictionaryEntry] = db.withSession( implicit s => DEs.byIdQuery(id).firstOption )


  def fetchById(id: Int, s: Session): Optional[DictionaryEntry] =
    transactionForSession(s)(implicit s => DictionaryEntries.byIdQuery(id).firstOption)

  def fetchByKeyword(keyword: String): JList[DictionaryEntry] = db.withSession(implicit s =>
    DEs.byKeywordQuery(keyword).list)

  def fetchByKeyword(keyword: String, s: Session): JList[DictionaryEntry] =
    transactionForSession(s)(implicit s => DEs.byKeywordQuery(keyword).list)

  def fetchBySourceLanguage(language: Language): JList[DictionaryEntry] = db.withSession(implicit s =>
    DEs.bySourceLanguageQuery(language).list)

  def fetchBySourceLanguage(language: Language, s: Session): JList[DictionaryEntry] =
    transactionForSession(s)(implicit s => DEs.bySourceLanguageQuery(language).list)

  def fetchByKeywordAndGroupId(keyword: String, groupId: Byte): Optional[DictionaryEntry] =
    db.withSession(implicit s => DEs.byKeywordAndKWGIndexQuery(keyword, groupId).firstOption)

  def fetchByKeywordAndGroupId(keyword: String, groupId: Byte, s: Session): Optional[DictionaryEntry] =
    transactionForSession(s)(implicit s => DEs.byKeywordAndKWGIndexQuery(keyword, groupId).firstOption)

  def fetchByKeywordAndSourceLanguage(keyword: String, lang: Language): List[DictionaryEntry] =
    db.withSession(implicit s => DEs.byKeywordAndSourceLanguageQuery(keyword, lang).list)

  def fetchByKeywordAndSourceLanguage(keyword: String, lang: Language, s: Session): List[DictionaryEntry] =
    transactionForSession(s)(implicit s => DEs.byKeywordAndSourceLanguageQuery(keyword, lang).list)

  def fetchAll(): JList[DictionaryEntry] = db.withSession(implicit s => Query(DEs).list)

  def fetchAll(s: Session): JList[DictionaryEntry] = transactionForSession(s)(implicit s => Query(DEs).list)

  def fetchKeywordsForLanguage(lang: Language): JList[String] =
    db.withSession( implicit s => DEs.keywordsForLanguageQuery(lang).list )

  def fetchKeywordsForLanguage(lang: Language, s: Session): JList[String] =
      transactionForSession(s)( implicit s => DEs.keywordsForLanguageQuery(lang).list )

  def keywordExists(keyword: String, lang: Language): Boolean =
    db.withSession( implicit s => DEs.byKeywordAndSourceLanguageQuery(keyword, lang).exists.run )

  def keywordExists(keyword: String, lang: Language, s: Session): Boolean =
    transactionForSession(s)( implicit s => DEs.byKeywordAndSourceLanguageQuery(keyword, lang).exists.run )
}

case class DictionaryEntry /*protected[scalaimpl]*/ (id: Option[Int],
                                                 sourceLanguage: Byte,
                                                 var prevId: Option[Int],
                                                 var nextId: Option[Int],
                                                 @BeanProperty var keywordGroupIndex: Byte,
                                                 @BeanProperty var keyword: String,
                                                 @BeanProperty var description: String,
                                                 var htmlDescription: Option[String],
                                                 var posIdx: Option[Byte])
  extends DictionaryEntryBean with  IDictionaryEntry {

  def contentsEqual(other: DictionaryEntry) = {
    def equalize(de: DictionaryEntry) = de.copy(id = None, prevId = None, nextId = None, keywordGroupIndex = 0)
    equalize(this) == equalize(other)
  }
}
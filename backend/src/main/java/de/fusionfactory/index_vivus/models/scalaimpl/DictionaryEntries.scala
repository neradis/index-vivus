package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs}
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.services.scalaimpl._


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

object DictionaryEntries extends Table[DictionaryEntry]("DICTIONARY_ENTRIES") {
  
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def sourceLanguage = column[Byte]("LANGUAGE")

  def prevId = column[Option[Int]]("PREV_ID")

  def nextId = column[Option[Int]]("NEXT_ID")

  def keywordGroupIndex = column[Byte]("KEYWORD_GROUP_INDEX")

  def keyword = column[String]("KEYWORD")

  def description = column[String]("DESCRIPTION")

  def htmlDescription = column[Option[String]]("HTML_DESCRIPTION")

  def pos = column[Option[Byte]]("POS")

  def baseProjection = sourceLanguage ~ prevId ~ nextId ~ keywordGroupIndex ~ keyword ~ description ~ htmlDescription ~ pos

  def prevIdFK = foreignKey("PREV_ID_FK", prevId, DictionaryEntries)(_.id)

  def nextIdFK = foreignKey("NEXT_ID_FK", nextId, DictionaryEntries)(_.id)

  def keywordIndex = index("KEYWORD_INDEX", keyword)

  def * = id.? ~: baseProjection <> (t => DictionaryEntry(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9),
    DictionaryEntry.unapply)

  def forInsert = baseProjection <>
    ((l, pi, ni, kgi, kw, d, hd, p) => DictionaryEntry.apply(None, l ,pi, ni, kgi, kw, d, hd, p),
      (e: DictionaryEntry) => Some((e.sourceLanguage, e.prevId, e.nextId, e.keywordGroupIndex,
        e.keyword, e.description, e.htmlDescription, e.posIdx)))

  def byIdQuery(id: Int) = Query(DEs).filter(_.id === id)

  def byKeywordQuery(kw: String) = Query(DEs).filter(_.keyword === kw)

  def bySourceLanguageQuery(lang: Language) =  Query(DEs) filter ( _.sourceLanguage === lang2Byte(lang) )

  def keywordsForLanguageQuery(lang: Language) =
    Query(DEs) filter (de => de.sourceLanguage === lang2Byte(lang)) map (de => de.keyword) 

  def byKeywordAndKWGIndexQuery(kw: String, kwgi: Byte) =
    Query(DEs) filter (de => (de.keyword === kw) && (de.keywordGroupIndex === kwgi))

  def byKeywordAndSourceLanguageQuery(kw: String, lang: Language) =
    Query(DEs) filter (de => (de.keyword === kw) && (de.sourceLanguage === lang2Byte(lang)))
}

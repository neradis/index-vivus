package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs}


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

object DictionaryEntries extends Table[DictionaryEntry]("DICTIONARY_ENTRY") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def prevId = column[Option[Int]]("PREV_ID")

  def nextId = column[Option[Int]]("NEXT_ID")

  def keywordGroupIndex = column[Byte]("KEYWORD_GROUP_INDEX")

  def keyword = column[String]("KEYWORD")

  def description = column[String]("DESCRIPTION")

  def htmlDescription = column[Option[String]]("HTML_DESCRIPTION")

  def pos = column[Option[Byte]]("POS")

  def baseProjection = prevId ~ nextId ~ keywordGroupIndex ~ keyword ~ description ~ htmlDescription ~ pos

 def prevIdFK = foreignKey("PREV_ID_FK", prevId, DictionaryEntries)(_.id)

  def nextIdFK = foreignKey("NEXT_ID_FK", nextId, DictionaryEntries)(_.id)

  def * = id.? ~: baseProjection <>(t => DictionaryEntry(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8),
    DictionaryEntry.unapply)

  def forInsert = baseProjection <>
    ((pi, ni, kgi, kw, d, hd, p) => DictionaryEntry(None, pi, ni, kgi, kw, d, hd, p),
      (e: DictionaryEntry) => Some((e.prevId, e.nextId, e.keywordGroupIndex,
        e.keyword, e.description, e.htmlDescription, e.posIdx)))

  def byIdQuery(id: Int) = Query(DEs).filter(_.id === id)

  def byKeywordQuery(kw: String) = Query(DEs).filter(_.keyword === kw)

  def byKeywordAndKWGIndexQuery(kw: String, kwgi: Byte) =
    Query(DEs) filter (de => (de.keyword === kw) && (de.keywordGroupIndex === kwgi))
}

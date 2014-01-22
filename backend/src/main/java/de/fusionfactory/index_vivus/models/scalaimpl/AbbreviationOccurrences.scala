package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{AbbreviationOccurrences => AbbOccs}
import scala.slick.lifted.ForeignKeyAction._


/**
 * 
 * 
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object AbbreviationOccurrences extends Table[(Int, Int)]("ABBREVIATION_OCCURRENCES"){

  def entryId = column[Int]("ENTRY_ID")

  def abbreviationId = column[Int]("ABBREVIATION_ID")

  def pk = primaryKey("PRIMARY_KEY", (entryId, abbreviationId))

  def entryIdFK = foreignKey("ENTRY_ID_FK", entryId, DictionaryEntries)(_.id, onDelete = Cascade)

  def abbrevIdFK = foreignKey("ABBREVIATION_ID_FK", abbreviationId, Abbreviations)(_.id, onDelete = Cascade)
  
  def * = entryId ~ abbreviationId

  def byIdsQuery(entryId: Int, abbrId: Int) =
    Query(AbbOccs).filter(ao => (ao.entryId === entryId) && (ao.abbreviationId === abbrId) )
}

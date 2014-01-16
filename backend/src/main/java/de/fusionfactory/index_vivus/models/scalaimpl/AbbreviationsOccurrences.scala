package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{AbbreviationsOccurrences => AbbOccs}


/**
 * 
 * 
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object AbbreviationsOccurrences extends Table[(Int, Int)]("ABBREVIATION_OCCURRENCES"){

  def entryId = column[Int]("ENTRY_ID")

  def abbreviationId = column[Int]("ABBREVIATION_ID")

  def pk = primaryKey("PRIMARY_KEY", (entryId, abbreviationId))

  def entryIdFK = foreignKey("ENTRY_ID_FK", entryId, DictionaryEntries)(_.id)

  def abbrevIdFK = foreignKey("ABBREVIATION_ID_FK", abbreviationId, Abbreviations)(_.id)
  
  def entryIdUnique = index("ENTRY_ID_INDEX", entryId, unique = true)

  def * = entryId ~ abbreviationId
}

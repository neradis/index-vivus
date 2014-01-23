package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._

import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs, GermanTokens => GTs}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
object GermanTokens extends Table[(String, Boolean)]("GERMAN_TOKENS") {

  def token = column[String]("TOKEN", O.PrimaryKey)

  def isGerman = column[Boolean]("IS_GERMAN")

  def * = token ~ isGerman

  def byTokenQuery(token: String) = Query(GTs).filter(_.token === token)
}

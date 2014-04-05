package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{Abbreviations => Abbr}
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.services.scalaimpl._

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object Abbreviations extends Table[Abbreviation]("ABBREVIATIONS"){

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def sourceLanguage = column[Byte]("LANGUAGE")

  def shortForm = column[String]("SHORT_FORM")

  def longForm = column[String]("LONG_FORM")

  def shortFormUnique  = index("SHORT_FORM_UNIQUE", shortForm, unique = true)

  def * = id.? ~: baseProjection <> ((id,sl,sf,lf) => Abbreviation(id, sl, sf, lf), Abbreviation.unapply)

  def baseProjection = sourceLanguage ~ shortForm ~ longForm

  def forInsert = baseProjection <> ((sl, sf, lf) => Abbreviation(sl,sf, lf),
                                     (abbr:Abbreviation) => Some((abbr.sourceLanguage, abbr.shortForm, abbr.longForm)))

  def byIdQuery(id: Int) = Query(Abbr).filter(_.id === id)

  def byShortFormQuery(sf: String) = Query(Abbr).filter(_.shortForm === sf)

  def byLanguageQuery(lang: Language) = Query(Abbr).filter(_.sourceLanguage === lang2Byte(lang))
}

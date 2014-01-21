package de.fusionfactory.index_vivus.models.scalaimpl

import scala.slick.driver.H2Driver.simple._
import de.fusionfactory.index_vivus.models.scalaimpl.{Abbreviations => Abbr}

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object Abbreviations extends Table[Abbreviation]("ABBREVIATIONS"){

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def shortForm = column[String]("SHORT_FORM")

  def longForm = column[String]("LONG_FORM")

  def shortFormUnique  = index("SHORT_FORM_UNIQUE", shortForm, unique = true)

  def longFormUnique  = index("LONG_FORM_UNIQUE", longForm, unique = true)

  def * = id.? ~: baseProjection <> ((id,sf,lf) => Abbreviation(id, sf, lf), Abbreviation.unapply)

  def baseProjection = shortForm ~ longForm

  def forInsert = baseProjection <> ((sf, lf) => Abbreviation(sf, lf),
                                     (abbr:Abbreviation) => Some((abbr.shortForm, abbr.longForm)))

  def byIdQuery(id: Int) = Query(Abbr).filter(_.id === id)

  def byShortFormQuery(sf: String) = Query(Abbr).filter(_.shortForm === sf)
}


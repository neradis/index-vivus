package de.fusionfactory.index_vivus.models.scalaimpl

import scala.beans.BeanProperty

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */


object Abbreviation {

  def apply(shortForm: String, longForm: String): Abbreviation =  apply(None, shortForm, longForm)

  def create(shortForm: String, longForm: String): Abbreviation = apply(shortForm, longForm)

}


case class Abbreviation protected[scalaimpl] (id: Option[Int],
                                              @BeanProperty shortForm: String,
                                              @BeanProperty longForm: String) {

  def contentsEqual(other: Abbreviation) = {
    def equalize(abbr: Abbreviation) = abbr.copy(id = None)
    equalize(this) == equalize(other)
  }
}

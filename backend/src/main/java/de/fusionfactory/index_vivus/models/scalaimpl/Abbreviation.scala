package de.fusionfactory.index_vivus.models.scalaimpl

import scala.beans.BeanProperty
import com.google.common.base.Optional
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db, _}
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntries => DEs, Abbreviations => Abbr}
import java.util.{List => JList}
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import scala.slick.session.Session
import scala.collection.convert.wrapAll._
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */


object Abbreviation {

  def apply(shortForm: String, longForm: String): Abbreviation =  apply(None, shortForm, longForm)

  def create(shortForm: String, longForm: String): Abbreviation = apply(shortForm, longForm)

  def fetchById(id: Int): Optional[Abbreviation] = db.withSession( implicit s => Abbr.byIdQuery(id).firstOption )


  def fetchById(id: Int, s: Session): Optional[Abbreviation] =
    transactionForSession(s)( implicit s => Abbr.byIdQuery(id).firstOption )

  def fetchByShortForm(keyword: String): Optional[Abbreviation] = db.withSession(implicit s =>
    Abbr.byShortFormQuery(keyword).firstOption)

  def fetchByShortForm(keyword: String, s: Session): Optional[Abbreviation] =
    transactionForSession(s)(implicit s => Abbr.byShortFormQuery(keyword).firstOption)

  def fetchAll(): JList[Abbreviation] = db.withSession( implicit s => Query(Abbr).list )

  def fetchAll(s: Session): JList[Abbreviation] = transactionForSession(s)( implicit s => Query(Abbr).list )
}

case class Abbreviation protected[scalaimpl] (id: Option[Int],
                                              @BeanProperty shortForm: String,
                                              @BeanProperty longForm: String)
  extends AbbreviationBean {

  def contentsEqual(other: Abbreviation) = {
    def equalize(abbr: Abbreviation) = abbr.copy(id = None)
    equalize(this) == equalize(other)
  }
}

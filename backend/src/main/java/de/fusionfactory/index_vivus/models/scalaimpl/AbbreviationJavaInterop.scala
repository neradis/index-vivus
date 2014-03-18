package de.fusionfactory.index_vivus.models.scalaimpl

import de.fusionfactory.index_vivus.models.ICrudOpsProvider
import scala.slick.session.Session
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import scala.collection.convert.wrapAll._
import java.util.{List => JList}
import de.fusionfactory.index_vivus.persistence.ORMError
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.services.scalaimpl._

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
trait AbbreviationBean extends ICrudOpsProvider[Abbreviation, AbbreviationCrudOps] { this: Abbreviation =>

  def getId = id.get

  def getIdOption = id

  def getLanguage: Language = sourceLanguage

  def crud(): AbbreviationCrudOps = new AbbreviationCrudOps(this)

  def crud(tx: Session): AbbreviationCrudOps = new AbbreviationCrudOps(this, Some(tx))
}

class AbbreviationCrudOps(val abbr: Abbreviation, val transaction: Option[Session] = None)
  extends CrudOps[Abbreviation] {


  def insertAsNew(): Abbreviation = opTransaction( implicit t => {
      val id = Abbreviations.forInsert.returning(Abbreviations.id).insert(abbr)
      abbr.copy(id = Some(id))
    }
  )

  def update(): Int = opTransaction( implicit t => {
    if(abbr.id.isEmpty) throw new ORMError(s"cannot update $abbr with undefined id")
    Abbreviations.byIdQuery(abbr.id.get) update abbr
  })


  def delete(): Int = opTransaction( implicit t => {
    if(abbr.id.isEmpty) throw new ORMError(s"cannot delete $abbr with undefined id")
    Abbreviations.byIdQuery(abbr.id.get) delete
  })


  def duplicateList(): JList[Abbreviation] =  {
    val sameShortFormAbbrevs = opTransaction( implicit t => Abbreviations.byShortFormQuery(abbr.shortForm).list )
    sameShortFormAbbrevs filter abbr.contentsEqual
  }
}
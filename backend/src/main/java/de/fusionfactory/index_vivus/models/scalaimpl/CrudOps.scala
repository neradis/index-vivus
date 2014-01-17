package de.fusionfactory.index_vivus.models.scalaimpl

import de.fusionfactory.index_vivus.models.ICrudOps
import scala.slick.session.Session
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}
import scala.Some

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
trait CrudOps[T] extends ICrudOps[T] { this  : { val transaction: Option[Session] } =>

  protected def opTransaction[T](work: Session => T): T = transaction match {
    case None => db.withTransaction(work)
    case Some(tr: Session) => tr.withTransaction(work(tr))
  }
}

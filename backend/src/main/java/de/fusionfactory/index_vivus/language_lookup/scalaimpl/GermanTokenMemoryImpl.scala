package de.fusionfactory.index_vivus.language_lookup.scalaimpl

import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}
import scala.slick.driver.H2Driver.simple.{Session => H2Session, _}
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import java.lang.{Boolean => JBoolean}

import com.google.common.base.Optional

import java.lang.Boolean
import de.fusionfactory.index_vivus.models.scalaimpl.GermanTokens

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class GermanTokenMemoryImpl{

  def isGerman(token: String): Optional[JBoolean] =
    db.withSession(implicit s => GermanTokens.byTokenQuery(token).map(_.isGerman).firstOption)

  def hasResult(token: String): Boolean =
    db.withSession(implicit s => GermanTokens.byTokenQuery(token).exists.run)

  def put(token: String, isGerman: Boolean) = {
    val work: H2Session => Unit = implicit s => {
      val isPresent = GermanTokens.byTokenQuery(token).exists.run
      if (isPresent)
        GermanTokens.byTokenQuery(token).update((token, isGerman))
      else
        GermanTokens.insert((token, Boolean2boolean(isGerman)))
    }
    db.withTransaction(work)
  }
}

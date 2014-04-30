package de.fusionfactory.index_vivus.services.scalaimpl

import de.fusionfactory.index_vivus.services.{Language, IAbbreviationSetsService}

import scala.concurrent._
import ExecutionContext.Implicits.global


import java.util.{Map => JMap}
import spray.caching.{Cache, LruCache}
import org.apache.log4j.Logger
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation
import scala.collection.convert.wrapAll._
import com.google.common.collect.Maps
import scala.concurrent.duration.Duration
import de.fusionfactory.index_vivus.xmlimport.Importer


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object AbbreviationSetsService {
  private lazy val logger = Logger.getLogger(classOf[AbbreviationSetsService])
  private lazy val instance = new AbbreviationSetsService

  def getInstance = instance
}


class AbbreviationSetsService private() extends IAbbreviationSetsService{

  lazy val abbrSetCache: Cache[JMap[String, String]] = LruCache(initialCapacity = 4)

  override def getAbbreviationExpansions(language: Language): JMap[String, String] = {

    val fetchMap: JMap[String, String] = {
      val list = Abbreviation.fetchByLanguage(language).toList
      val origPairs = list.map(a => a.shortForm -> a.longForm)
      val toStrip = origPairs.filter(_ match {
        case (sf: String, lf: String) if Importer.testIfShortFormDotStrippable(sf, language) => true
        case _ => false
      })
      val stripped = toStrip.map {
        case (sf, lf) => sf.take(sf.length - 1) -> lf
      }

      (origPairs ++ stripped).foldLeft(Maps.newHashMap[String, String]())((m, p) => {
        m.put(p._1, p._2); m
      })
    }

    Await.result( abbrSetCache(language, () => future(fetchMap) ), Duration.Inf )
  }
}

package de.fusionfactory.index_vivus.services.scalaimpl

import de.fusionfactory.index_vivus.services.{Language, IAbbreviationSetsService}

import scala.concurrent._
import ExecutionContext.Implicits.global


import java.util.{Map => JMap}
import spray.caching.LruCache
import org.apache.log4j.Logger
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation
import scala.collection.convert.wrapAll._
import com.google.common.collect.Maps
import scala.concurrent.duration.Duration

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

  lazy val abbrSetCache = LruCache.apply[JMap[String, String]](initialCapacity = 4)

  override def getAbbreviationExpansions(language: Language): JMap[String, String] = {

    val fetchMap = {
      val list = Abbreviation.fetchByLanguage(language)
      list.foldLeft[JMap[String, String]](Maps.newHashMapWithExpectedSize(list.size())){ (res, abbr) =>
        res.put(abbr.shortForm, abbr.longForm)
        res
      }
    }

    Await.result( abbrSetCache(language, () => future(fetchMap) ), Duration.Inf )
  }
}

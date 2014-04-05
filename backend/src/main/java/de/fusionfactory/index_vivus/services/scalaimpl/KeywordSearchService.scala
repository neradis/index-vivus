package de.fusionfactory.index_vivus.services.scalaimpl

import de.fusionfactory.index_vivus.services.{Language, IKeywordSearchService}
import java.util.{List => JList}
import scala.collection.mutable.{Map => MutaMap}
import de.fusionfactory.index_vivus.spellchecker.SpellChecker
import scala.collection.convert.wrapAll._
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}
import de.fusionfactory.index_vivus.models.scalaimpl.{DictionaryEntry => DE }
import org.apache.log4j.Logger
import spray.caching.LruCache
import scala.concurrent.{Await, future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object KeywordSearchService extends App{
  private lazy val logger = Logger.getLogger(classOf[KeywordSearchService])
  private lazy val instance = new KeywordSearchService()

  def getInstance = instance


  /*logger.debug("requesting completion")
  instance.getCompletions("dom", Language.LATIN)
  logger.debug("starting wait loop till completion ready")
  val sw = new Stopwatch().start()
  while( !instance.autocompletionReady(Language.LATIN)) {
    Thread.sleep(100) //TODO: check if blocking really is really required
  }
  sw.stop()
  logger.debug(s"waited ${sw.elapsedTime(TimeUnit.MILLISECONDS)} ms")*/
}


class KeywordSearchService private() extends IKeywordSearchService {

  lazy val completerCache = LruCache.apply[SpellChecker](initialCapacity = 4)

  def fetchCompleter(language: Language) = Await.result(
    completerCache(language, () => future(new SpellChecker(language))), Duration.Inf)

  def getMatches(keyword: String, language: Language) = DE.fetchByKeywordAndSourceLanguage(keyword, language)

  def getCompletions(keyword: String, language: Language): JList[String] = {
    fetchCompleter(language).getAutocompleteSuggestions(keyword).toList
  }

  def autocompletionReady(language: Language): Boolean = fetchCompleter(language).autoCompletionReady

  def alternativesReady(language: Language): Boolean = fetchCompleter(language).alternativesReady
}

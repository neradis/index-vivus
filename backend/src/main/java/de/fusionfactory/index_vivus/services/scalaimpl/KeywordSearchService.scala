package de.fusionfactory.index_vivus.services.scalaimpl

import de.fusionfactory.index_vivus.services.{Language, IKeywordSearchService}
import java.util.{List => JList}
import de.fusionfactory.index_vivus.spellchecker.SpellChecker
import scala.collection.convert.wrapAll._
import de.fusionfactory.index_vivus.persistence.SlickTools.{database => db}
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry
import org.apache.log4j.Logger


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object KeywordSearchService {
  private lazy val logger = Logger.getLogger(classOf[KeywordSearchService])
  private lazy val instance = new KeywordSearchService()

  def getInstance = instance
}


class KeywordSearchService private() extends IKeywordSearchService {

  lazy val completers: Map[Language, SpellChecker] = Map.empty.withDefault(l => new SpellChecker(l))


  def getMatches(keyword: String, language: Language) = DictionaryEntry.fetchByKeyword(keyword)

  def getCompletions(keyword: String, language: Language): JList[String] = {
    completers(language).getAutocompleteSuggestions(keyword).toList
  }
}

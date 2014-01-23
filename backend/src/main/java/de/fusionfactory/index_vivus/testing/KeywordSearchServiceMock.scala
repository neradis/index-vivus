package de.fusionfactory.index_vivus.testing

import de.fusionfactory.index_vivus.services.{Language, IKeywordSearchService}
import de.fusionfactory.index_vivus.services.Language.{LATIN, GREEK}
import scala.collection.convert.wrapAsJava._
import de.fusionfactory.index_vivus.models.{WordType, ICrudOps, IDictionaryEntry}
import com.google.common.base.Optional

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
class KeywordSearchServiceMock extends IKeywordSearchService{

  private lazy val latinWords = List("et", "in", "est", "non", "ut", "ad", "cum", "quod", "si", "qui", "quae", "sed", 
    "quam","esse", "aut", "ex", "de", "nec", "etiam", "sunt", "se", "enim", "hoc", "atque", "me", "per", "te", "ab", 
    "quid", "id", "ne", "ac", "autem", "agricola", "bestia", "bellum", "dominus", "dominare")


  private lazy val greekWords = List("many", "nice", "interesting", "greek", "words")

  private lazy val wordsByLanguage = Map(LATIN -> latinWords, GREEK -> greekWords)


  def getMatches(kw: String, lang: Language) = wordsByLanguage(lang).filter(kw == _) map (w => dictEntryStub(w, lang))

  def getCompletions(kw: String, lang: Language) = wordsByLanguage(lang).filter(_.startsWith(kw))

  private def dictEntryStub(kw: String, lang: Language) = new IDictionaryEntry {

    def getLanguage: Language = Language.LATIN

    def setDescription(description: String): Unit = ???

    def setKeyword(keyword: String): Unit = ???

    def getPreviousEntryId: Optional[Integer] = ???

    def setNextEntryId(id: Optional[Integer]): Unit = ???

    def getWordType: WordType = WordType.UNKNOWN

    def getHtmlDescription: Optional[String] = Optional.absent()

    val getId: Int = wordsByLanguage(lang).indexOf(kw)

    def getDescription: String = s"the meaning for $kw"

    def setWordType(wordType: WordType): Unit = ???

    def getNextEntryId: Optional[Integer] = ???

    def crud(): ICrudOps[_ <: IDictionaryEntry] = ???

    def setPreviousEntryId(id: Optional[Integer]): Unit = ???

    def getKeyword: String = kw

    def getKeywordGroupIndex: Byte = 1

    def setKeywordGroupIndex(keywordGroupIndex: Byte): Unit = ???

    def setHtmlDescription(description: Optional[String]): Unit = ???

    override def toString: String =
      s"id: $getId, keyword: $getKeyword, description: $getDescription, html: $getHtmlDescription"
  }
}

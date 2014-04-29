package de.fusionfactory.index_vivus.xmlimport

import org.scalatest.{FlatSpec, BeforeAndAfter}
import de.fusionfactory.index_vivus.configuration.Environment
import de.fusionfactory.index_vivus.persistence.{SlickTools, DbHelper}
import de.fusionfactory.index_vivus.testing.fixtures.LoadFixtures
import de.fusionfactory.index_vivus.services.scalaimpl.AbbreviationSetsServiceTest
import org.ahocorasick.trie.{Emit, Trie}
import scala.collection.convert.wrapAll._
import scala.math._
import ImporterTest._
import Function.tupled
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry
import scala.slick.session.Session
import de.fusionfactory.index_vivus.services.Language
import org.apache.log4j.Logger
import scala.collection.mutable.ListBuffer

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object ImporterTest {

  lazy val logger = Logger.getLogger(classOf[ImporterTest])
  lazy val markedOccPattern = "<abbr>[^<]+?</abbr>".r
  lazy val shortForms = AbbreviationSetsServiceTest.georgesAbbrSelection.keys.toSet
  lazy val shortFormTrie = shortForms.foldLeft(new Trie().removeOverlaps()) {
    (trie, str) => trie.addKeyword(str); trie
  }
}

class ImporterTest extends FlatSpec with BeforeAndAfter {


  before {
    if (Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()
    }
  }

  def abbrOccTemplate(coords: Emit => (Int, Int))(text: String) = {
    shortFormTrie.parseText(text).toSeq.map(emit => tupled[Int, Int, String](text.substring(_, _))(coords(emit)))
  }

  def expandCoords(text: String)(emit: Emit): (Int, Int) = (max(emit.getStart - 6, 0), min(emit.getEnd + 8, text.length))

  def abbrOccs(text: String) = abbrOccTemplate(emit => (emit.getStart, emit.getEnd))(text)

  def abbrOccsWithContext(text: String) = abbrOccTemplate(expandCoords(text))(text)

  "The importer" should "add <abbr>-tags around occurring abbreviations in Latin entries" in {

    var lastMarked: Option[String] = None
    val missing = ListBuffer.empty[String]

    SlickTools.database.withTransaction {
      implicit s: Session =>
        for {
          entry <- DictionaryEntry.fetchBySourceLanguage(Language.LATIN)
          eid = entry.id.get
          text <- List(entry.getDescription, entry.getHtmlDescription.or(""))
          (occ, occWithContext) <- abbrOccs(text).zip(abbrOccsWithContext(text))
        } {
          occWithContext match {
            case occ: String if markedOccPattern.findFirstMatchIn(occ).isDefined => lastMarked = Some(occ)
            case s => if (missing.size < 10) missing += s"No tag for $occ in $occWithContext (entry id: $eid)"
          }
        }
    }

    if (missing.nonEmpty)
      fail(lastMarked map (_ => s"Some abbreviation marks missing:\n${missing.mkString("\n")}") getOrElse
          s"No abbreviation tags at all! Some of the missed ones are:\n${missing.mkString("\n")}")
  }

  val expCharsMap = Map(
    "g.{1,2}nzlich" -> "gänzlich",
    "geh.{1,2}ren" -> "gehören",
    "urspr.{1,2}nglich" -> "ursprünglich",
    "schlie.{1,2}t" -> "schließt"
  )

  val expCharsRegex = expCharsMap.keys.map(p => s"(?:$p)") mkString "|" r
  val expWords = expCharsMap.values.toSet

  it should "import Greek entries with correct umlauts and special characters" in {
    for {
      entry <- DictionaryEntry.fetchBySourceLanguage(Language.GREEK)
      text <- List(entry.getDescription, entry.getHtmlDescription.or(""))
      occ <- expCharsRegex.findAllIn(text)
    } if (!expWords.contains(occ)) fail(s"Wrong umlaut: $occ")
  }
}

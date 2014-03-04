package de.fusionfactory.index_vivus.services.scalaimpl

import org.scalatest.{BeforeAndAfter, FlatSpec}
import de.fusionfactory.index_vivus.services.Language
import scala.collection.convert.wrapAll._
import scala.collection.convert.decorateAll._
import de.fusionfactory.index_vivus.configuration.Environment
import de.fusionfactory.index_vivus.configuration.Environment._
import de.fusionfactory.index_vivus.persistence.DbHelper
import de.fusionfactory.index_vivus.testing.fixtures.LoadFixtures
import org.apache.log4j.Logger


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
object KeywordSearchServiceTest {
  val logger = Logger.getLogger(classOf[KeywordSearchServiceTest])
}

class KeywordSearchServiceTest extends FlatSpec with BeforeAndAfter {

  import KeywordSearchServiceTest._

  protected case class Expectations(completionQuery: String, completionResults: Set[String], matching: Set[String])

  val expectedCompletions = Map(
    DEVELOPMENT -> Expectations("dom", Set("dominare", "dominus"), Set("dominare, dominus")),
    TEST -> Expectations("aba", Set("abacinus", "abactus", "abacus"), Set("abacinus", "abacus"))
  )

  def testData = expectedCompletions(Environment.getActive)

  before {
    if (Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()
    }
  }

  "The keyword search service" should "be accessible" in {
    assert(KeywordSearchService.getInstance != null)
  }

  it should s"provide Latin completions for '${testData.completionQuery}'" in {
    val kwService = KeywordSearchService.getInstance

    val earlyCompletions = kwService.getCompletions(testData.completionQuery, Language.LATIN)

    Thread.sleep(2000) // wait to give Spellchecker a chance to complete it's async initialization
    val laterCompletions = kwService.getCompletions(testData.completionQuery, Language.LATIN).asScala.toSet
    logger.debug(s"expected completions: ${testData.completionResults.mkString(", ")}")
    logger.debug(s"actual completions: ${laterCompletions.mkString(", ")}")
    logger.debug(s"missing completions: ${(testData.completionResults -- laterCompletions).mkString(", ")}")

    assert(testData.completionResults subsetOf laterCompletions)
  }

  it should s"provide the keywords ${testData.matching.mkString(", ")} directly as match" in {
    for (keyword <- testData.matching) {
      val matches = KeywordSearchService.getInstance.getMatches(keyword, Language.LATIN)
      assert(!matches.isEmpty)
      assert(matches.forall(_.keyword == keyword))
    }
  }
}

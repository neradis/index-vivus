package de.fusionfactory.index_vivus.services.scalaimpl

import org.scalatest.{BeforeAndAfter, FlatSpec}
import de.fusionfactory.index_vivus.services.Language
import scala.collection.convert.wrapAll._
import scala.collection.convert.decorateAll._
import de.fusionfactory.index_vivus.configuration.Environment
import de.fusionfactory.index_vivus.persistence.DbHelper
import de.fusionfactory.index_vivus.testing.fixtures.LoadFixtures

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class KeywordSearchServiceTest extends FlatSpec with BeforeAndAfter{

  before {
    if(Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()
    }
  }

  "The keyword search service" should "be accessible" in {
    assert(KeywordSearchService.getInstance != null)
  }

  it should "provide Latin completions for 'dom'" in {
    val earlyCompletions = KeywordSearchService.getInstance.getCompletions("dom", Language.LATIN)

    Thread.sleep(2000) // wait to give Spellchecker a chance to complete it's async initialization
    val laterCompletions = KeywordSearchService.getInstance.getCompletions("dom", Language.LATIN)
    assert(Set("dominare", "dominus") subsetOf laterCompletions.asScala.toSet)
  }

  it should "provide the entry for 'dominus' as match" in {
    val matches = KeywordSearchService.getInstance.getMatches("dominus", Language.LATIN)
    assert( !matches.isEmpty)
    assert( matches.forall(_.keyword == "dominus"))
  }

}

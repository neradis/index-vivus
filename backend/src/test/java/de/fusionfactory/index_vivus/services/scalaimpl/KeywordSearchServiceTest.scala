package de.fusionfactory.index_vivus.services.scalaimpl

import org.scalatest.FlatSpec
import de.fusionfactory.index_vivus.services.Language
import scala.collection.convert.wrapAll._
import scala.collection.convert.decorateAll._

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class KeywordSearchServiceTest extends FlatSpec {



  "The keyword search service" should "be accessible" in {
    assert(KeywordSearchService.getInstance != null)
  }

  it should "provide Latin completions for 'dom'" in {
    val earlyCompletions = KeywordSearchService.getInstance.getCompletions("dom", Language.LATIN)
    Thread.sleep(2000)
    val laterCompletions = KeywordSearchService.getInstance.getCompletions("dom", Language.LATIN)
    assert(Set("dominare", "dominus") subsetOf laterCompletions.asScala.toSet)
  }

  it should "provide the entry for 'dominus' as match" in {
    val matches = KeywordSearchService.getInstance.getMatches("dominus", Language.LATIN)
    assert( !matches.isEmpty)
    assert( matches.forall(_.keyword == "dominus"))
  }

}

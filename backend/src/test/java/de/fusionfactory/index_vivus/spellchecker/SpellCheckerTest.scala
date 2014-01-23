package de.fusionfactory.index_vivus.spellchecker

import org.scalatest.FlatSpec
import de.fusionfactory.index_vivus.services.Language
import scala.collection.convert.wrapAll._

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class SpellCheckerTest extends FlatSpec {

  "The completion service" should "offer sound completions/corrections (dominare/dominus)" in {
    val sc = new  SpellChecker(Language.LATIN)
    assertResult("dominus")(sc.getBestAlternativeWord("dominvs"))
    assertResult("dominare")(sc.getBestAlternativeWord("dominave"))
    assert(Set("dominare", "dominus") subsetOf sc.getAutocompleteSuggestions("dom").toSet)
  }
}

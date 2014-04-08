package de.fusionfactory.index_vivus.spellchecker

import org.scalatest.{BeforeAndAfter, FlatSpec}
import de.fusionfactory.index_vivus.services.Language
import scala.collection.convert.wrapAll._
import de.fusionfactory.index_vivus.configuration.Environment
import de.fusionfactory.index_vivus.persistence.DbHelper
import de.fusionfactory.index_vivus.testing.fixtures.LoadFixtures

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class SpellCheckerTest extends FlatSpec  with BeforeAndAfter {

  before {
    if(Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()
    }
  }

  "The completion service" should "offer sound corrections (dominare/dominus) in dev env" in {
    if(Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()

      val sc = new  SpellChecker(Language.LATIN)
      Thread.sleep(1000) // wait to give Spellchecker a chance to complete it's async initialization

      "dominus" === sc.getBestAlternativeWord("dominvs")
      "dominare" === sc.getBestAlternativeWord("dominave")
      assert(Set("dominare", "dominus") subsetOf sc.getAutocompleteSuggestions("dom").toSet)
    }
  }


}

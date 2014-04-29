package de.fusionfactory.index_vivus.services.scalaimpl

import org.scalatest.{BeforeAndAfter, FlatSpec}
import de.fusionfactory.index_vivus.configuration.Environment
import de.fusionfactory.index_vivus.persistence.DbHelper
import de.fusionfactory.index_vivus.testing.fixtures.{FixtureData, LoadFixtures}
import de.fusionfactory.index_vivus.configuration.Environment._
import scala.collection.convert.wrapAll._
import com.google.common.collect.Maps
import AbbreviationSetsServiceTest._
import de.fusionfactory.index_vivus.services.Language


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object AbbreviationSetsServiceTest {

  lazy val georgesAbbrSelection = Map(
    "a. a. O." -> "am angeführten Orte.",
    "allg." -> "allgemein.",
    "in ders. Bed." -> "in derselben Bedeutung.",
    "meton." -> "metonymisch.",
    "tr." -> "transitivum od. transitive.",
    "vorkl." -> "vorklassisch.",
    "vorz." -> "vorzüglich.",
    "W." -> "Wort.",
    "WW." -> "Wörter.",
    "Zshg." -> "Zusammenhang.",
    "Zustz." -> "Zusatz.",
    "zugl." -> "zugleich.",
    "zuw." -> "zuweilen.",
    "zw." -> "zweifelhaft."
  )

  lazy val fixutreAbbr = (FixtureData.ABBREVIATIONS.foldLeft(Maps.newHashMap[String, String]) {
    (res, abbr) => res.put(abbr.shortForm, abbr.longForm); res
  }).toMap


  lazy val expectedAbbreviations = Map(
    DEVELOPMENT -> fixutreAbbr,
    TEST -> georgesAbbrSelection,
    PRODUCTION -> georgesAbbrSelection
  )
}


class AbbreviationSetsServiceTest extends FlatSpec with BeforeAndAfter {


  before {
    if (Environment.getActive == Environment.DEVELOPMENT) {
      DbHelper.createMissingTables()
      LoadFixtures.createFixtures()
    }
  }

  "The abbreviation set service" should "give correct an complete listings of abbreviations and their expansions" in {

    val expected = expectedAbbreviations(Environment.getActive)

    val actual = AbbreviationSetsService.getInstance.getAbbreviationExpansions(Language.LATIN)

    expected.entrySet subsetOf actual.entrySet

  }
}

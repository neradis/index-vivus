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
import java.util


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object AbbreviationSetsServiceTest {

  lazy val georgesAbbrSelection: util.Map[String, String] = List(
    "a. a. O." -> "am angeführten Orte",
    "allg." -> "allgemein",
    "allg" -> "allgemein",
    "in ders. Bed." -> "in derselben Bedeutung",
    "meton." -> "metonymisch",
    "meton" -> "metonymisch",
    "tr." -> "transitivum oder transitive",
    "tr" -> "transitivum oder transitive",
    "vorkl." -> "vorklassisch",
    "vorkl" -> "vorklassisch",
    "vorz." -> "vorzüglich",
    "W." -> "Wort",
    "WW." -> "Wörter",
    "Zshg." -> "Zusammenhang",
    "Zshg" -> "Zusammenhang",
    "Zustz." -> "Zusatz",
    "zugl." -> "zugleich",
    "zuw." -> "zuweilen",
    "zw." -> "zweifelhaft"
  ).foldLeft(Maps.newHashMap[String, String])((m, p) => {
    m.put(p._1, p._2); m
  })

  lazy val fixutreAbbr: util.Map[String, String] = FixtureData.ABBREVIATIONS.foldLeft(Maps.newHashMap[String, String])(
    (m, a) => {
      m.put(a.getShortForm, a.getLongForm); m
    })


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

    expected.keySet().toList.foreach(ek => assert(actual.get(ek) == expected.get(ek), (ek, actual.get(ek), expected.get(ek))))
  }
}

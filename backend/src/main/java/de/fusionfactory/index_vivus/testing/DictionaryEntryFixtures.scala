package de.fusionfactory.index_vivus.testing

import de.fusionfactory.index_vivus.models.ModelFactory
import de.fusionfactory.index_vivus.persistence.DictionaryEntryDAO
import circumflex.orm
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import org.apache.log4j.Logger
import com.google.common.base.Optional

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object DictionaryEntryFixtures extends App {
  private val logger = Logger.getLogger(this.getClass)
  private val dao = DictionaryEntryDAO.getInstance

  def createFixtureObjects = {
    val entryData = List(
      ("index", "das Verzeichnis, der Zeigefinder"),
      ("index verborum", "Wörterbuch"),
      ("verbum", "das Wort, der Spruch")
    )

    val fixturesNotLoaded = entryData.map(_._1).forall(dao.findByKeyword(_).isEmpty)
    if (fixturesNotLoaded) {
      var prevEntry: Option[DictionaryEntry] = None
      for ((kw, desc) <- entryData) {
        val entry = ModelFactory.createDictionaryEntry(kw, desc)
        entry.setPreviousEntry(prevEntry)

        dao.insertDictionaryEntry(entry)
        logger.info(s"inserted $entry with prev link to ${prevEntry}")

        prevEntry match {
          case Some(prevEntry) => {
            prevEntry.setNextEntry(Optional.of(entry))
            dao.updateDictionaryEntry(prevEntry)
          }
          case _ => ()
        }

        prevEntry = Some(entry.asInstanceOf[DictionaryEntry])
      }
    }
    orm.tx.commit()
  }

  createFixtureObjects
}

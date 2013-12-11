package de.fusionfactory.index_vivus.testing

import de.fusionfactory.index_vivus.models.ModelFactory
import de.fusionfactory.index_vivus.persistence.DictionaryEntryCircumFlexDAO
import circumflex.orm
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry
import de.fusionfactory.index_vivus.tools.scala.Utils.OptionConversions._
import org.apache.log4j.Logger

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object DictionaryEntryFixtures extends App{
  private val logger = Logger.getLogger(this.getClass)
  private val dao = DictionaryEntryCircumFlexDAO.getInstance

  def createFixtureObjects = {
    val entryData = List(("index", "das Verzeichnis, der Zeigefinder"),
                         ("index verborum", "Wörterbuch"),
                         ("verbum", "das Wort, der Spruch"))

    var prevEntry: Option[DictionaryEntry] = None
    for((kw, desc) <- entryData) {
      val entry = ModelFactory.createDictionaryEntry(kw, desc)
      if(dao.findByKeyword(kw).isEmpty) {

        val lastId = prevEntry match {
          case Some(e) => e.id.map(int2Integer)
          case None => None
        }

        logger.info(s"last id value: ${(DictionaryEntry.lastIdValue, dao.getLastIdValue)}")

        entry.setPreviousEntryId(prevEntry.map(_.id.get) match {
          case Some(opt @ Some(i)) => opt
          case _ => None
        })
        logger.info(s"last id value found: ${lastId}")
        entry.setPreviousEntryId(dao.getLastIdValue)
        dao.insertDictionaryEntry(entry)

        logger.info(s"inserted $entry")
        orm.tx.commit()
      }
    }

    val indexEntry = dao.findByKeyword("index")
    val indexEntryById = dao.findById(1)

    println(s"index is first element: ${indexEntry.get(0) == indexEntryById.get()}")

    orm.tx.commit()
    println(indexEntry)
  }

  createFixtureObjects
}

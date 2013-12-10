package de.fusionfactory.index_vivus.testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonFactory
import com.google.common.io.Resources
import scala.collection.JavaConversions._
import java.util.{List => JList, Iterator => JIterator}
import de.fusionfactory.index_vivus.persistence.PersistenceProvider
import de.fusionfactory.index_vivus.persistence.PersistenceProvider.Work
import javax.persistence.EntityManager
import de.fusionfactory.index_vivus.models.DictionaryEntry
import org.apache.log4j.Logger

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object FixturesLoader extends App{
  lazy protected val logger = Logger.getLogger(FixturesLoader.getClass)
  lazy protected val jsonFactory = new JsonFactory()
  lazy protected val mapper = new ObjectMapper()

  new FixturesLoader[DictionaryEntry](classOf[DictionaryEntry]).saveFixtruesObjectsFrom("fixtures/DictionaryEntry.json")
}


class FixturesLoader[T](val class_ : Class[T]) {

  import FixturesLoader._

  def loadFixtureObjects(resourceName: String): JIterator[T] = {
    mapper.readValues(jsonFactory.createParser(Resources.getResource(resourceName)), class_)
  }

  def saveFixtruesObjectsFrom(resourceName: String) = {
    PersistenceProvider.INSTANCE.performTransaction(new Work[Unit] {
      protected def doWork(em: EntityManager): Unit = for (obj <- loadFixtureObjects(resourceName)) {
        logger.debug(s"Persisting fixture object: $obj")
        em.persist(obj)
      }
    })
  }
}

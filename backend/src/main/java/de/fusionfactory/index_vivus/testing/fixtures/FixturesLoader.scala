package de.fusionfactory.index_vivus.testing.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonFactory
import com.google.common.io.Resources
import java.util.{List => JList, Iterator => JIterator}
import org.apache.log4j.Logger

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object FixturesLoader {
  lazy protected val logger = Logger.getLogger(FixturesLoader.getClass)
  lazy protected val jsonFactory = new JsonFactory()
  lazy protected val mapper = new ObjectMapper()
}

class FixturesLoader[T](val class_ : Class[T]) {

  import FixturesLoader._

  def loadFixtureObjects(resourceName: String): JIterator[T] =
    mapper.readValues(jsonFactory.createParser(Resources.getResource(resourceName)), class_)
}

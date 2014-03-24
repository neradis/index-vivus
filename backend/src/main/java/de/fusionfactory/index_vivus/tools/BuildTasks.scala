package de.fusionfactory.index_vivus.tools

import de.fusionfactory.index_vivus.persistence.DbHelper
import de.fusionfactory.index_vivus.testing.fixtures
import de.fusionfactory.index_vivus.xmlimport.Importer
import de.fusionfactory.index_vivus.indexer.Indexer
import de.fusionfactory.index_vivus.persistence.SlickTools.{dbFilesExist, deleteDbFiles}

import BuildTasks._
import de.fusionfactory.index_vivus.configuration.LocationProvider
import java.io.File
import org.apache.log4j.Logger
import org.apache.commons.io.FileUtils
import com.aliasi.io.FileExtensionFilter


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */


object BuildTasks {

  lazy val logger = Logger.getLogger(BuildTasks.getClass)
  lazy val indexDir = new File(LocationProvider.getInstance.getDataDir, "index.lucene.bin") 
  
  def initDb = DbHelper.createMissingTables()

  def loadFixtures = fixtures.LoadFixtures.createFixtures()

  def importDictionaries = Importer.main(Array.empty)

  def createFulltextIndex = {
    fixtures.LoadFixtures.ensureFixturesForDevelopment()
    new Indexer().createIndex()
  }

  def indexExists = indexDir.isDirectory
  
  def deleteIndeces = {
    LocationProvider.getInstance.getDataDir.listFiles(new FileExtensionFilter(false, "model")) foreach ( _.delete() )
    FileUtils.deleteDirectory(indexDir)
  }
}

object InitDb extends App {
  initDb
}

object LoadFixtures extends App {
  initDb
  loadFixtures
}

object ImportDictionaries extends App {
  initDb
  importDictionaries
}

object CreateFulltextIndex extends App {
  createFulltextIndex
}

object CleanDb extends App {
  deleteDbFiles
}

object CleanIndices extends App {
  deleteIndeces
}

object CleanDbAndIndices extends App {
  deleteDbFiles
  deleteIndeces
}

object CreateAll extends App {
  if( !dbFilesExist ) {
    logger.info("Creating database and importing entries...")
    initDb
    importDictionaries
  }
  if( !indexExists ) {
    logger.info("Creating fulltext index...")
    createFulltextIndex
  }
}

object ReCreateAll extends App {
  logger.info("Purging db and index files...")
  deleteDbFiles
  deleteIndeces
  logger.info("Creating database and importing entries...")
  initDb
  importDictionaries
  logger.info("Creating fulltext index...")
  createFulltextIndex
}

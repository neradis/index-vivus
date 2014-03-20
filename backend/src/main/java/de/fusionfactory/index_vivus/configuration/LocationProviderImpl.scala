package de.fusionfactory.index_vivus.configuration

import com.typesafe.config.{ConfigException, ConfigFactory, Config}
import java.io.File
import com.google.common.io.Resources
import scala.util.{Failure, Success, Try}

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
class LocationProviderImpl extends LocationProvider {

  val BUILD_PROPERTY_FILE = "build.properties"

  lazy val buildProps : Config =  {
    Try(Resources.getResource(BUILD_PROPERTY_FILE)) match { //check if file is available in classpath
      case Success(_) => ConfigFactory.load(BUILD_PROPERTY_FILE)
      case Failure(ex: IllegalArgumentException) =>
        throw new ConfigException.BadPath(BUILD_PROPERTY_FILE, "cannot be located in classpath")
      case Failure(ex) => throw ex
    }
  }

  override def getProjectRoot = new File(buildProps.getString("project.rootDir"))
  override def getProjectBuild = new File(buildProps.getString("project.buildDir"))
  override def getBackendRoot = new File(buildProps.getString("backend.rootDir"))
  override def getBackendBuild = new File(buildProps.getString("backend.buildDir"))

  override def getDataDir = ensureDir(getBackendRoot, s"data/${Environment.getActive.name}")

  override def getInputDir = ensureDir(getBackendRoot, s"inputs/${Environment.getActive.name}")

  override def getDictionaryDir = ensureDir(getInputDir, "dictionaries")

  override def getIndexDir: File = ensureDir(getDataDir, "lucene_index")

  protected def ensureDir(root: File, relPath: String) = {
    val dir = new File(root, relPath)
    if( !dir.isDirectory) assert(dir.mkdirs())
    dir
  }
}

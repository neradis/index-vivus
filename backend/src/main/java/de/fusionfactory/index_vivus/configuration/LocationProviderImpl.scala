package de.fusionfactory.index_vivus.configuration

import com.typesafe.config.{ConfigFactory, Config}
import java.io.File

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
class LocationProviderImpl extends LocationProvider {
  lazy val buildProps : Config = ConfigFactory.load("build.properties")

  override def getProjectRoot = new File(buildProps.getString("project.rootDir"))
  override def getProjectBuild = new File(buildProps.getString("project.buildDir"))
  override def getBackendRoot = new File(buildProps.getString("backend.rootDir"))
  override def getBackendBuild = new File(buildProps.getString("backend.buildDir"))

  override def getDataDir = ensureDir(getBackendRoot, s"data/${Environment.getActive.name}")

  override def getInputDir = ensureDir(getBackendRoot, s"inputs/${Environment.getActive.name}")

  override def getDictionaryDir = ensureDir(getInputDir, "dictionaries")

  protected def ensureDir(root: File, relPath: String) = {
    val dir = new File(root, relPath)
    if( !dir.isDirectory) assert(dir.mkdirs())
    dir
  }
}

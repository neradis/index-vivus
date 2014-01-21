package de.fusionfactory.index_vivus.configuration

import com.typesafe.config.{ConfigFactory, Config}
import java.io.File

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
class LocationProviderImpl extends LocationProvider {
  lazy val buildProps : Config = ConfigFactory.load("build.properties")

  override def getProjectRoot: File = new File(buildProps.getString("project.rootDir"))
  override def getProjectBuild: File = new File(buildProps.getString("project.buildDir"))
  override def getBackendRoot: File = new File(buildProps.getString("backend.rootDir"))
  override def getBackendBuild: File = new File(buildProps.getString("backend.buildDir"))

  override def getDataDir: File = new File(getBackendRoot, s"data/${Environment.getActive.name}")
}

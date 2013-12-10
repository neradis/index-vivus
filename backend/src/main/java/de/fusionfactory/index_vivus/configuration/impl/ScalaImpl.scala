package de.fusionfactory.index_vivus.configuration.impl

import de.fusionfactory.index_vivus.configuration.Environment
import java.util.{List => JList}

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object ScalaImpl {

    def getActiveEnvironment(envVars : JList[String]) = {
      val envs = List("RAILS_ENV", "ENV").map(sys.env.get).filter(_.isDefined).map(x => Environment.byString(x.get))
      envs.toSet.toList match {
        case Nil => Environment.DEFAULT_ENVIRONMENT
        case List(env) => env
        case List(names @ _*) => throw new Environment.AmbiguousEnvironmentException(names.map(_.name) : _*)
      }
  }
}
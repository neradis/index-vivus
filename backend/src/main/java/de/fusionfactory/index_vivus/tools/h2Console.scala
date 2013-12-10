package de.fusionfactory.index_vivus.tools

import org.h2.tools.{Console => H2Console}
import de.fusionfactory.index_vivus.configuration.SettingsProvider

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object h2Console extends App{


  H2Console.main("-url", SettingsProvider.getInstance.getDatabaseUrl)
}

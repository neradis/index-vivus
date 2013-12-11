package de.fusionfactory.index_vivus.persistence.scala

import de.fusionfactory.index_vivus.configuration.SettingsProvider
import circumflex.core.cx
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry
import circumflex.orm.DDLUnit


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object CircumflexORMInit {
  lazy val tablesToInitialize = List(DictionaryEntry)

  lazy val ensureConfigured = {
    cx.put("orm.connection.url", SettingsProvider.getInstance.getDatabaseUrl)
    for(table <- tablesToInitialize) {
      new DDLUnit(table).CREATE()
    }

    true
  }
}

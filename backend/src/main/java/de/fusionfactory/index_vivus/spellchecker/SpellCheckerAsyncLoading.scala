package de.fusionfactory.index_vivus.spellchecker

import org.apache.log4j.Logger
import scala.concurrent.{future, ExecutionContext}
import ExecutionContext.Implicits.global
import com.google.common.base.Optional


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class SpellCheckerAsyncLoading(host: SpellChecker) {
  import SpellCheckerAsyncLoading.logger

  logger.debug(s"submit keywordsFuture (${host.language}})")
  val keywordsFuture = future (host.fetchKeywords() )

  val completerFuture = keywordsFuture map( host.createAutoCompleter )
  completerFuture.onSuccess { case completer => host.autoCompleterPromise = Optional.of(completer) }
  completerFuture.onFailure { case t: Throwable =>
    throw new IllegalArgumentException("error creating the completer", t) }

  val spellcheckerFuture = keywordsFuture map ( host.provideCompiledSpellCheckerModel )
  spellcheckerFuture.onSuccess { case spellchecker => host.spellCheckerPromise = Optional.of(spellchecker) }
  spellcheckerFuture.onFailure { case t: Throwable =>
    throw new IllegalArgumentException("error creating the spellchecker", t) }


  logger.debug(s"all futures submitted (${host.language})")
}

object SpellCheckerAsyncLoading {

  lazy val logger = Logger.getLogger(classOf[SpellCheckerAsyncLoading])
}

package de.fusionfactory.index_vivus.tools;

import de.fusionfactory.index_vivus.tools.scala.Utils$;

import java.io.File;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class Utils {

  private static Utils$ scalaUtils = Utils$.MODULE$;

  static File fileForResource(String resourceName) {

    return scalaUtils.fileForResouce(resourceName);
  }
}

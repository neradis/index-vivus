package de.fusionfactory.index_vivus.persistence;

import de.fusionfactory.index_vivus.persistence.scala.CircumFlexORMInit$;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class CircumFlexORMInitializer {

    public static boolean ensureConfigured() {
        return CircumFlexORMInit$.MODULE$.ensureConfigured();
    }
}

package de.fusionfactory.index_vivus.persistence;

import de.fusionfactory.index_vivus.persistence.scala.CircumflexORMInit$;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class CircumflexORMInitializer {

    public static boolean ensureConfigured() {
        return CircumflexORMInit$.MODULE$.ensureConfigured();
    }
}

package de.fusionfactory.index_vivus.persistence;

import scala.runtime.AbstractFunction1;
import scala.slick.session.Database;
import scala.slick.session.Session;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class DbHelper {

    private final static SlickTools$  slickTools= SlickTools$.MODULE$;

    public static Database getDatabase() {
        return slickTools.database();
    }

    public static <T> T transaction(Operations<T> ops) {

        final Operations<T> opsForClosure = ops;
        return getDatabase().withTransaction(new AbstractFunction1<Session, T>() {

            @Override
            public T apply(Session tr) {
                return opsForClosure.perform(tr);
            }
        });
    }

    public static abstract class Operations<T> {

        public abstract T perform(Session transaction);

    }

    public static void createMissingTables() {
        slickTools.createMissingTables();
    }
}

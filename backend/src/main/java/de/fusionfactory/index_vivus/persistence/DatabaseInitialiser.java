package de.fusionfactory.index_vivus.persistence;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class DatabaseInitialiser {

    public static void main(String[] args) {
        DbHelper.createMissingTables();
    }
}

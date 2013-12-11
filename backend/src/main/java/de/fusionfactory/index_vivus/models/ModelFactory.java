package de.fusionfactory.index_vivus.models;

import de.fusionfactory.index_vivus.persistence.CircumflexORMInitializer;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class ModelFactory {
    static {
        if( !CircumflexORMInitializer.ensureConfigured() ) {
            throw new RuntimeException("Circumflex init failed");
        }
    }


    public static IDictionaryEntry createDictionaryEntry(String keyword, String description, int keywordGroupIndex) {
        return new de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry(keyword,
                description, keywordGroupIndex);
    }

    public static IDictionaryEntry createDictionaryEntry(String keyword, String description) {
        return createDictionaryEntry(keyword, description, 1);
    }
}

package de.fusionfactory.index_vivus.models;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class ModelFactory {

    public static IDictionaryEntry createDictionaryEntry(String keyword, String description) {
        return new  de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry(keyword, description);
    }
}

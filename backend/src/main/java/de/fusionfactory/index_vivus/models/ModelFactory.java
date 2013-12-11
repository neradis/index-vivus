package de.fusionfactory.index_vivus.models;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class ModelFactory {

    public static IDictionaryEntry createDictionaryEntry(String keyword, String description, int keywordGroupIndex) {
        return new de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry(keyword,
                description, keywordGroupIndex);
    }

    public static IDictionaryEntry createDictionaryEntry(String keyword, String description) {
        return createDictionaryEntry(keyword, description, 1);
    }
}

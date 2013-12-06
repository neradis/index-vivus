package de.fusionfactory.indexvivus.model;

import com.google.common.base.Optional;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:08
 */
public class DictionaryEntry {
    private int id;
    private String keyword;
    private String description;
    private WordType wordType;
    private int nextId;
    private int prevId;
    private int[] related;
    private int keywordGroupIndex;

    private DictionaryEntry(int id, String keyword, String description, WordType wordType, int nextId, int prevId, int[] related, int keywordGroupIndex) {
        this.id = id;
        this.keyword = keyword;
        this.description = description;
        this.wordType = wordType;
        this.nextId = nextId;
        this.prevId = prevId;
        this.related = related;
        this.keywordGroupIndex = keywordGroupIndex;
    }

    public static DictionaryEntry byID(int id) {
        return null;
    }

    public int getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }

    public WordType getWordType() {
        return wordType;
    }

    public Optional<DictionaryEntry> getPreviousEntry() {
        return getEntry(prevId);
    }

    public Optional<DictionaryEntry> getNextEntry() {
        return getEntry(nextId);
    }

    public int getKeywordGroupIndex() {
        return keywordGroupIndex;
    }

    public List<DictionaryEntry> getRelated() {
        throw new UnsupportedOperationException("getRelated is not implemented yet.");
    }

    private Optional<DictionaryEntry> getEntry(int id) {
        Optional<DictionaryEntry> entry;

        if (id > 0)
            entry = Optional.of(byID(id));
        else
            entry = Optional.absent();

        return entry;
    }

}

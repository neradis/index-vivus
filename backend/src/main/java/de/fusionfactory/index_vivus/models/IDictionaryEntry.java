package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface IDictionaryEntry {
    int getId();

    String getKeyword();

    void setKeyword(String keyword);

    String getDescription();

    void setDescription(String description);

    WordType getWordType();

    void setWordType(WordType wordType);

    Optional<? extends IDictionaryEntry> getPreviousEntry();

    void setPreviousEntry(Optional<? extends IDictionaryEntry> entry);

    Optional<Integer> getPreviousEntryId();

    void setPreviousEntryId(Optional<Integer> id);

    Optional<? extends IDictionaryEntry> getNextEntry();

    void setNextEntry(Optional<? extends IDictionaryEntry> entry);

    Optional<Integer> getNextEntryId();

    void setNextEntryId(Optional<Integer> id);

    int getKeywordGroupIndex();

    void setKeywordGroupIndex(int keywordGroupIndex);

    List<? extends IDictionaryEntry> getRelated() throws UnsupportedOperationException;
}

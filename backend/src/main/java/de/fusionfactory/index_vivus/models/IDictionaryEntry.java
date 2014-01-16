package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface IDictionaryEntry {

    public int getId();

    public String getKeyword();

    public void setKeyword(String keyword);

    public String getDescription();

    public void setDescription(String description);

    public Optional<String> getHtmlDescription();

    public void setHtmlDescription(Optional<String> description);

    public WordType getWordType();

    public void setWordType(WordType wordType);

    public Optional<? extends IDictionaryEntry> getPreviousEntry();

    public void setPreviousEntry(Optional<? extends IDictionaryEntry> entry);

    public Optional<Integer> getPreviousEntryId();

    public void setPreviousEntryId(Optional<Integer> id);

    public Optional<? extends IDictionaryEntry> getNextEntry();

    public void setNextEntry(Optional<? extends IDictionaryEntry> entry);

    public Optional<Integer> getNextEntryId();

    public void setNextEntryId(Optional<Integer> id);

    public int getKeywordGroupIndex();

    public void setKeywordGroupIndex(int keywordGroupIndex);

    public List<? extends IDictionaryEntry> getRelated() throws UnsupportedOperationException;

    public ICrudOps<? extends IDictionaryEntry> crud();
}

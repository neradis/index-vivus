package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;

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

    public Optional<Integer> getPreviousEntryId();

    public void setPreviousEntryId(Optional<Integer> id);

    public Optional<Integer> getNextEntryId();

    public void setNextEntryId(Optional<Integer> id);

    public byte getKeywordGroupIndex();

    public void setKeywordGroupIndex(byte keywordGroupIndex);

    public ICrudOps<? extends IDictionaryEntry> crud();
}

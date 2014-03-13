package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.services.Language;
import scala.slick.session.Session;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface IDictionaryEntry {

    public int getId();

    public Language getLanguage();

    public String getKeyword();

    public void setKeyword(String keyword);

    public String getDescription();

    public void setDescription(String description);

    public Optional<String> getHtmlDescription();

    public void setHtmlDescription(Optional<String> description);

    public WordType getWordType();

    public void setWordType(WordType wordType);

    public Optional<Integer> getPreviousEntryId();

    public Optional<? extends IDictionaryEntry> getPreviousEntry();

    public void setPreviousEntryId(Optional<Integer> id);

    public Optional<Integer> getNextEntryId();

    public Optional<? extends IDictionaryEntry> getNextEntry();

    public void setNextEntryId(Optional<Integer> id);

    public byte getKeywordGroupIndex();

    public void setKeywordGroupIndex(byte keywordGroupIndex);

    public List<? extends IDictionaryEntry> getRelated();

    public List<? extends Abbreviation> getOccurringAbbreviations();

    public List<? extends Abbreviation> getOccurringAbbreviations(Session s);

    public ICrudOps<? extends IDictionaryEntry> crud();
}

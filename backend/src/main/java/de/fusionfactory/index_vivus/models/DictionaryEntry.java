package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.persistence.PersistenceProvider;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.util.List;

import static de.fusionfactory.index_vivus.persistence.PersistenceProvider.Work;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:08
 */
@Entity
public class DictionaryEntry /*implements IDictionaryEntry*/ {
    @Id
    private int id = Integer.MIN_VALUE;
    private String keyword;
    private String description;
    private WordType wordType;
    private Optional<Integer> nextId;
    private Optional<Integer> prevId;
    private List<Integer> related;
    private int keywordGroupIndex;

    public DictionaryEntry(int id, String keyword, String description, WordType wordType, Optional<Integer> prevId,
                           Optional<Integer> nextId, List<Integer> related, int keywordGroupIndex) {
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

        final Integer id_ = Integer.valueOf(id);

        return PersistenceProvider.INSTANCE.performTransaction(new Work<DictionaryEntry>() {

            //@Override
            protected DictionaryEntry doWork(EntityManager em) {

                return em.find(DictionaryEntry.class, Integer.valueOf(id_));
            }
        });
    }

    //@Override
    public int getId() {
        return id;
    }

    //@Override
    public String getKeyword() {
        return keyword;
    }

    //@Override
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    //@Override
    public String getDescription() {
        return description;
    }

    //@Override
    public void setDescription(String description) {
        this.description = description;
    }

    //@Override
    public WordType getWordType() {
        return wordType;
    }

    //@Override
    public void setWordType(WordType wordType) {
        this.wordType = wordType;
    }

    //@Override
    public Optional<DictionaryEntry> getPreviousEntry() {
        return getEntry(prevId);
    }

    //@Override
    public Optional<Integer> getPreviousEntryId() {
        return null;
    }

    //@Override
    public void setPreviousEntryId(Optional<Integer> id) {
        this.prevId = id;
    }

    //@Override
    public Optional<DictionaryEntry> getNextEntry() {
        return getEntry(nextId);
    }

    //@Override
    public Optional<Integer> getNextEntryId() {
        return null;
    }

    //@Override
    public void setNextEntryId(Optional<Integer> id) {
        this.nextId = id;
    }

    //@Override
    public int getKeywordGroupIndex() {
        return keywordGroupIndex;
    }

    //@Override
    public void setKeywordGroupIndex(int keywordGroupIndex) {
        this.keywordGroupIndex = keywordGroupIndex;
    }

    //@Override
    public List<DictionaryEntry> getRelated() {
        throw new UnsupportedOperationException("getRelated is not implemented yet.");
    }

    private Optional<DictionaryEntry> getEntry(Optional<Integer> id) {
        Optional<DictionaryEntry> entry;

        if (id.isPresent())
            entry = Optional.of(byID(id.get()));
        else
            entry = Optional.absent();

        return entry;
    }
}

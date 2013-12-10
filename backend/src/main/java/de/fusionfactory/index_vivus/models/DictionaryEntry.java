package de.fusionfactory.index_vivus.models;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.persistence.PersistenceProvider;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

import static de.fusionfactory.index_vivus.persistence.PersistenceProvider.Work;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:08
 */
@Entity
public class DictionaryEntry {
    @Id @GeneratedValue
    private long id = Integer.MIN_VALUE;
    private String keyword;
    private String description;
    private WordType wordType;
    private int nextId;
    private int prevId;
    private List<Integer> related;
    private int keywordGroupIndex;

    public DictionaryEntry(String keyword, String description, WordType wordType, int nextId, int prevId,
                           List<Integer> related, int keywordGroupIndex) {
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

            @Override
            protected DictionaryEntry doWork(EntityManager em) {

                return em.find(DictionaryEntry.class, Integer.valueOf(id_));
            }
        });
    }

    public long getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WordType getWordType() {
        return wordType;
    }

    public void setWordType(WordType wordType) {
        this.wordType = wordType;
    }

    public Optional<DictionaryEntry> getPreviousEntry() {
        return getEntry(prevId);
    }

    public void setPreviousEntryId(int id) {
        this.prevId = id;
    }

    public Optional<DictionaryEntry> getNextEntry() {
        return getEntry(nextId);
    }

    public void setNextEntryId(int id) {
        this.nextId = id;
    }

    public int getKeywordGroupIndex() {
        return keywordGroupIndex;
    }

    public void setKeywordGroupIndex(int keywordGroupIndex) {
        this.keywordGroupIndex = keywordGroupIndex;
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

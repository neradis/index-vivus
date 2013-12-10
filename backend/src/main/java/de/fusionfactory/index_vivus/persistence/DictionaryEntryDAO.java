package de.fusionfactory.index_vivus.persistence;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.models.IDictionaryEntry;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry$;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class DictionaryEntryDAO {
    public static final DictionaryEntryDAO INSTANCE = new DictionaryEntryDAO();
    private static final DictionaryEntry$ scalaDictionaryEntry;

    static {
        if( !CircumflexORMInitializer.ensureConfigured() ) {
            throw new RuntimeException("Circumflex init failed");
        }
        scalaDictionaryEntry = DictionaryEntry$.MODULE$;
    }

    public static DictionaryEntryDAO getInstance() {
        return INSTANCE;
    }

    private DictionaryEntryDAO() {
    }

    public Optional<? extends IDictionaryEntry> findById(final int id) {
        return scalaDictionaryEntry.findByIdJava(id);
    }

    public List<? extends IDictionaryEntry> findByKeyword(String keyword) {
        return scalaDictionaryEntry.findByKeywordJava(keyword);
    }

    public void insertDictionaryEntry(IDictionaryEntry dictionaryEntry) {
        scalaDictionaryEntry.insertJava(dictionaryEntry);
    }

    public void updateDictionaryEntry(IDictionaryEntry dictionaryEntry) {
        scalaDictionaryEntry.updateJava(dictionaryEntry);
    }

    public Optional<Integer> getLastIdValue() {
        return scalaDictionaryEntry.lastIdValueJava();
    }
}
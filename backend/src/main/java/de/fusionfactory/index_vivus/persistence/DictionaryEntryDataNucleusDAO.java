package de.fusionfactory.index_vivus.persistence;

import de.fusionfactory.index_vivus.models.DictionaryEntry;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class DictionaryEntryDataNucleusDAO {

    public DictionaryEntry findById(final int id) {
        return PersistenceProvider.INSTANCE.performTransaction(new PersistenceProvider.Work<DictionaryEntry>() {
            @Override
            protected DictionaryEntry doWork(EntityManager em) {
                DictionaryEntry entry = em.find(DictionaryEntry.class, id);
                accessFieldsForActivation(entry);
                return entry;
            }
        });
    }

    public List<DictionaryEntry> findByKeyword(final String keyword) {
        return PersistenceProvider.INSTANCE.performTransaction(new PersistenceProvider.Work<List<DictionaryEntry>>() {
            @Override
            protected List<DictionaryEntry> doWork(EntityManager em) {
                TypedQuery<DictionaryEntry> query =
                    em.createQuery("SELECT de FROM DictionaryEntry de WHERE de.keyword = :keyword", DictionaryEntry.class);
                query.setParameter("keyword", keyword);
                List<DictionaryEntry> entries = query.getResultList();
                for(DictionaryEntry e : entries) {
                    accessFieldsForActivation(e);
                }
                return entries;
            }
        });
    }

    protected int getHighestExistingPrimaryKey(EntityManager em) {
        return em.createQuery("SELECT MAX(de.id) FROM DictionaryEntry de", Integer.class).getSingleResult();
    }


    protected void accessFieldsForActivation(DictionaryEntry entry) {
        entry.getWordType();
    }
}

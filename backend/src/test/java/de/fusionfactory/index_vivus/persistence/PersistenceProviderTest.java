package de.fusionfactory.index_vivus.persistence;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.models.DictionaryEntry;
import de.fusionfactory.index_vivus.models.WordType;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

import static de.fusionfactory.index_vivus.persistence.PersistenceProvider.Work;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class PersistenceProviderTest {

    @Test
    public void testPerformTransaction() throws Exception {
        PersistenceProvider.INSTANCE.performTransaction(new Work<Boolean>() {

            @Override
            protected Boolean doWork(EntityManager em) {
                DictionaryEntry de = new DictionaryEntry(1, "cognere", "wissen/kennen", WordType.VERB,
                        Optional.<Integer>absent(), Optional.of(2), new LinkedList<Integer>(), 1);
                em.persist(de);

                return true;
            }
        });

        PersistenceProvider.INSTANCE.performTransaction(new Work<Boolean>() {

            @Override
            protected Boolean doWork(EntityManager em) {
                CriteriaQuery<DictionaryEntry> cq = em.getCriteriaBuilder().createQuery(DictionaryEntry.class);
                Root<DictionaryEntry> root = cq.from(DictionaryEntry.class);
                cq.select(root);
                //List results = em.createQuery("SELECT e FROM DictionaryEntry e").getResultList();
                List<DictionaryEntry> results = em.createQuery(cq).getResultList();


                for (DictionaryEntry dictionaryEntry : results) {
                    System.out.println("Found entry for " + dictionaryEntry.getKeyword());
                }

                return true;
            }
        });
    }

    /*@Test
    public void testGetEntityManager() throws Exception {
        EntityManager em = PersistenceProvider.INSTANCE.getEntityManagerFactory();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DictionaryEntry de = new DictionaryEntry(42, "cognere", "wissen/kennen", WordType.VERB,
                                                     2, -1, new LinkedList<Integer>(), 1);
            em.persist(de);
            tx.commit();

        } finally {
            if( tx.isActive() ) {
                tx.rollback();
            }
        }
        em.close();
    }*/
}

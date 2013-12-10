package de.fusionfactory.index_vivus.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import de.fusionfactory.index_vivus.configuration.SettingsProvider;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Map;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class PersistenceProvider {
    private static Logger logger = Logger.getLogger(PersistenceProvider.class);

    public static final PersistenceProvider INSTANCE = new PersistenceProvider();

    private Optional<EntityManagerFactory> entityManagerFactory = Optional.absent();

    private PersistenceProvider() {
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if( !entityManagerFactory.isPresent() ) {
            Map<String, String> emfProps = ImmutableMap.of("javax.persistence.jdbc.url",
                                                           SettingsProvider.getInstance().getDatabaseUrl());
            logger.info(String.format("Using database url '%s'", SettingsProvider.getInstance().getDatabaseUrl()));
            entityManagerFactory = Optional.of(Persistence.createEntityManagerFactory("DictionaryModels", emfProps));

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        PersistenceProvider.INSTANCE.entityManagerFactory.get().close();
                    } catch (IllegalStateException ise) { /*noop - emf not loaded or it is closed already*/ }
                }
            }));
        }
        return entityManagerFactory.get();
    }

    public <T> T performTransaction(Work<T> work) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        T result = null;
        try {
            tx.begin();
            result = work.doWork(em);
            tx.commit();
        } finally {
            if(tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return result;
    }

    public static abstract class Work<T> {
        protected abstract T doWork(EntityManager em);
    }
}

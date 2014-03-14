package de.fusionfactory.index_vivus.testing.fixtures;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.Environment;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.AbbreviationOccurrence;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import org.apache.log4j.Logger;
import scala.slick.session.Session;

import java.util.List;

import static de.fusionfactory.index_vivus.persistence.DbHelper.Operations;
import static de.fusionfactory.index_vivus.persistence.DbHelper.createMissingTables;
import static de.fusionfactory.index_vivus.testing.fixtures.FixtureData.EntryDescription;
import static java.lang.String.format;
import static java.util.Map.Entry;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class LoadFixtures {
    transient private static Logger logger = Logger.getLogger(LoadFixtures.class);

    public static void createDictionaryEntryFixtures() {
        DbHelper.transaction(new Operations<Object>() {

            @Override
            public Object perform(Session tx) {
                Optional<DictionaryEntry> prevEntry = Optional.absent();
                for(DictionaryEntry de : FixtureData.DICTIONARY_ENTRIES) {
                    List<DictionaryEntry> duplicates = de.crud(tx).duplicateList();
                    if (duplicates.isEmpty()) {

                        if (prevEntry.isPresent()) {
                            de.setPreviousEntryId(Optional.of(prevEntry.get().getId()));
                        }

                        DictionaryEntry savedEntry = de.crud(tx).insertAsNew();

                        if (prevEntry.isPresent()) {
                            prevEntry.get().setNextEntryId(savedEntry.getIdOptional());
                            prevEntry.get().crud(tx).update();
                        }

                        prevEntry = Optional.of(savedEntry);
                    } else {
                        duplicateWarning("entries", de, duplicates);
                    }
                }
                return null; //nothing to return
            }
        });
    }

    public static void createAbbreviationFixtures() {
        DbHelper.transaction(new Operations<Object>() {

            @Override
            public Object perform(Session tx) {
                for(Abbreviation abbr : FixtureData.ABBREVIATIONS) {
                    List<Abbreviation> duplicates = abbr.crud(tx).duplicateList();
                    if(duplicates.isEmpty()) {
                        abbr.crud(tx).insertAsNew();
                    } else {
                        duplicateWarning("abbreviations", abbr, duplicates);
                    }
                }
                return null; //nothing to return
            }
        });
    }

    public static void addAbbreviationOccurrenceRelations() {
        DbHelper.transaction(new Operations<Object>() {

        @Override
        public Object perform(Session tx) {
            for(Entry<EntryDescription, String> hintEntry : FixtureData.ABBREVIATION_OCCURENCE_HINTS.entrySet()) {

                EntryDescription entryDesc = hintEntry.getKey();
                String abbrShortForm = hintEntry.getValue();

                DictionaryEntry entry = DictionaryEntry.fetchByKeywordAndGroupId(entryDesc.getKeyword(),
                        entryDesc.getKeywordGroupIndex(), tx).get();
                Abbreviation abbreviation = Abbreviation.fetchByShortForm(abbrShortForm, tx).get();

                if(AbbreviationOccurrence.exists(entry.getId(), abbreviation.getId(), tx)) {
                    logger.warn(format("Skipping already added abbreviation occurrence relation: %s#%d - %s",
                        entry.getKeyword(), entry.getKeywordGroupIndex(), abbreviation.getShortForm()));
                } else {
                    AbbreviationOccurrence.create(entry.getId(), abbreviation.getId(), tx);
                }
            }
            return null; //nothing to return
        }
    });
}

    protected static <T> void duplicateWarning(String fixtureDesc,T insertCandidate ,List<T> duplicates) {
        String dupListing = Joiner.on(" \n").join(duplicates);
        logger.warn(format("Already found %s in database with same content as %s:%n%s%n ==> SKIPPED",
                fixtureDesc, insertCandidate, dupListing));
    }

    public static void createFixtures() {
        createMissingTables();
        createDictionaryEntryFixtures();
        createAbbreviationFixtures();
    }

    public static void ensureFixturesForDevelopment() {
        if(Environment.getActive() == Environment.DEVELOPMENT){
            createFixtures();
        }
    }

    public static void main(String[] args) {
        createFixtures();
    }
}

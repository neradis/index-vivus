package de.fusionfactory.index_vivus.tools;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import org.apache.log4j.Logger;
import scala.slick.session.Session;

import java.util.List;

import static de.fusionfactory.index_vivus.persistence.DbHelper.Operations;
import static de.fusionfactory.index_vivus.persistence.DbHelper.createMissingTables;
import static java.lang.String.format;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class LoadFixtures {
    transient private static Logger logger = Logger.getLogger(LoadFixtures.class);

    public static Iterable<DictionaryEntry> fetchFixtures() {
        final Optional<Integer> integerAbsent = Optional.absent();
        final Optional<String> stringAbsent = Optional.absent();


        return ImmutableList.of(
                DictionaryEntry.create(integerAbsent, integerAbsent, (byte) 1,
                        "index", "das Verzeichnis, der Zeigefinder", stringAbsent, WordType.NOUN),
                DictionaryEntry.create(integerAbsent, integerAbsent, (byte) 1,
                        "index verborum", "das Wörterbuch", stringAbsent, WordType.NOUN),
                DictionaryEntry.create(integerAbsent, integerAbsent, (byte) 1,
                        "verbum", "das Wort, der Spruch", stringAbsent, WordType.NOUN),
                DictionaryEntry.create(integerAbsent, integerAbsent, (byte) 1,
                        "SPQR", "Abkürzung: Eigentum des römischen Sentats und des römischen Volkes",
                        stringAbsent, WordType.UNKNOWN)
        );
    }

    public static void addFixturesToDatabase() {
        DbHelper.transaction(new Operations<Object>() {

            @Override
            public Object perform(Session tx) {
                Optional<DictionaryEntry> prevEntry = Optional.absent();
                for (DictionaryEntry de : fetchFixtures()) {
                    List<DictionaryEntry> duplicates = de.crud(tx).duplicateList();
                    if (duplicates.isEmpty()) {


                        if (prevEntry.isPresent()) {
                            de.setPrevId(Optional.of(prevEntry.get().getId()));
                        }

                        DictionaryEntry savedEntry = de.crud(tx).insertAsNew();

                        if (prevEntry.isPresent()) {
                            prevEntry.get().setNextId(savedEntry.getIdOptional());
                            prevEntry.get().crud(tx).update();
                        }

                        prevEntry = Optional.of(savedEntry);
                    } else {
                        String dupList = Joiner.on(" \n").join(duplicates);
                        logger.warn(format("Already found entries in database with same content as %s:%n%s%n - SKIPPED",
                                de, dupList));
                    }
                }
                return null; //nothing to return
            }
        });
    }

    public static void main(String[] args) {
        createMissingTables();
        addFixturesToDatabase();
    }
}

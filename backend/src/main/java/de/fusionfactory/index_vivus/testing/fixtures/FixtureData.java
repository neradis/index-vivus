package de.fusionfactory.index_vivus.testing.fixtures;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;

import static de.fusionfactory.index_vivus.services.Language.LATIN;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class FixtureData {
   private static final Optional<Integer> integerAbsent = Optional.absent();
    private static final Optional<String> stringAbsent = Optional.absent();

    public static final ImmutableSet<DictionaryEntry> DICTIONARY_ENTRIES = ImmutableSet.of(
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "index", "das Verzeichnis", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 2,
                    "index", "der Zeigefinder (spezl. und genauer: index digitus)", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "index verborum", "das Wörterbuch", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "verbum", "das Wort, der Spruch", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "dominus", "der Herr, der Meister (substn. von dominare)", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "dominare", "beherrschen, kontrollieren", stringAbsent, WordType.VERB),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "beatus", "schön", stringAbsent, WordType.ADJECTIVE),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "SPQR", "abgekzt. für: Senātus Populusque Rōmānus Eigentum - " +
                            "des römischen Senats und des römischen Volkes", stringAbsent, WordType.OTHER
            )
    );

    public static final ImmutableSet<Abbreviation> ABBREVIATIONS = ImmutableSet.of(
            Abbreviation.create(LATIN, "abgekzt.", "abgekürzt"),
            Abbreviation.create(LATIN, "spezl.", "speziell"),
            Abbreviation.create(LATIN, "substn.", "substantivisch")
    );
    public static final ImmutableMap<EntryDescription, String> ABBREVIATION_OCCURENCE_HINTS = ImmutableMap.of(
            new EntryDescription("index", (byte) 2), "spezl.",
            new EntryDescription("dominus", (byte) 1), "substn.",
            new EntryDescription("SPQR", (byte) 1), "abgekzt."
    );

    public static class EntryDescription {

        final String keyword;
        final byte keywordGroupIndex;


        public EntryDescription(String keyword, byte keywordGroupIndex) {
            this.keyword = keyword;
            this.keywordGroupIndex = keywordGroupIndex;
        }

        public String getKeyword() {
            return keyword;
        }

        public byte getKeywordGroupIndex() {
            return keywordGroupIndex;
        }

    }
}




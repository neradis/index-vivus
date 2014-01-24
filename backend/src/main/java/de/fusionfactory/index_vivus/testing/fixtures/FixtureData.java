package de.fusionfactory.index_vivus.testing.fixtures;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;

import java.util.Set;

import static de.fusionfactory.index_vivus.services.Language.LATIN;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class FixtureData {
    private static final Optional<Integer> integerAbsent = Optional.absent();
    private static final Optional<String> stringAbsent = Optional.absent();


    public static final ImmutableSet<DictionaryEntry> DICTIONARY_ENTRIES = ImmutableSet.copyOf(makeFakeEntries());

    public static final ImmutableSet<DictionaryEntry> DICTIONARY_ENTRIES_REAL = ImmutableSet.of(
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "index", "das Verzeichnis", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 2,
                    "index", "der Zeigefinder (spez. und genauer: index digitus)", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "index verborum", "das Wörterbuch", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "verbum", "das Wort, der Spruch", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "dominus", "der Herr, der Meister (subst. von dominare)", stringAbsent, WordType.NOUN),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "dominare", "beherrschen, kontrollieren", stringAbsent, WordType.VERB),
            DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    "beatus", "schön", stringAbsent, WordType.ADJECTIVE)


    );

    static protected Set<DictionaryEntry> makeFakeEntries() {
        Set<DictionaryEntry> fakes = Sets.newHashSet();

        for(int i=0; i < 22; i++) {
            fakes.add(DictionaryEntry.create(LATIN, integerAbsent, integerAbsent, (byte) 1,
                    String.format("SPQR%d", 65+ i), "abgek. für: Senātus Populusque Rōmānus Eigentum - " +
                    "des römischen Senats und des römischen Volkes Haus Maus Laus Katze Mensch",
                    stringAbsent, WordType.OTHER));
        }
       /* fakes.addAll(DICTIONARY_ENTRIES_REAL);*/
        return fakes;
    }




    public static final ImmutableSet<Abbreviation> ABBREVIATIONS = ImmutableSet.of(
            Abbreviation.create("abgek.", "abgekürzt"),
            Abbreviation.create("spez.", "speziell"),
            Abbreviation.create("subst.", "substantivisch")
    );

    public static final ImmutableMap<EntryDescription, String> ABBREVIATION_OCCURENCE_HINTS = ImmutableMap.of(
            new EntryDescription("index", (byte) 2), "spez.",
            new EntryDescription("dominus", (byte) 1), "subst.",
            new EntryDescription("SPQR", (byte) 1), "abgek."
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

    public static void main(String[] args) {
        makeFakeEntries();
    }


}




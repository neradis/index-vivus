package de.fusionfactory.index_vivus.tokenizer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 13:50
 */
public class Tokenizer {

    //compiling Patterns is costly, but they have small memory footprint, so do it only once in init an keep them
    //TODO: we probably have to generalise the pattern for splitting into tokens a bit allowing for dashes, semicolon etc.
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern SECTION_MARKER_PATTERN = Pattern.compile("(?:(?:[IVX]+)|(?:[A-Z]))\\)");
    private static final Pattern GERMAN_WORD_PATTERN = Pattern.compile("[a-zA-ZäöüÄÖÜß]{2,}");


    private ImmutableMap<String, Abbreviation> abbrMap = buildAbbrMap();
    ;
    private Logger logger = Logger.getLogger(Tokenizer.class);
    ;

    public Tokenizer() {
	}

    private static ImmutableMap<String, Abbreviation> buildAbbrMap() {
        List<Abbreviation> abbrList = Abbreviation.fetchAll();
        Map<String, Abbreviation> result = Maps.newHashMapWithExpectedSize(abbrList.size());
        for (Abbreviation abbr : abbrList) {
            result.put(abbr.getShortForm(), abbr);
        }
        return ImmutableMap.copyOf(result);
    }

    public List<String> getTokenizedString(String entryText) {
        List<String> result = Lists.newLinkedList();

        String[] tokens = WHITESPACE_PATTERN.split(entryText);

        for (String token : tokens) {
            Optional<String> expansion = expandOrFilterToken(token);
            if (expansion.isPresent()) {
                result.add(expansion.get());
            }
        }

        return ImmutableList.copyOf(result);
    }

    private Optional<String> expandAbbreviation(String word) {
        return abbrMap.containsKey(word) ? Optional.of(abbrMap.get(word).getLongForm()) : Optional.<String>absent();
    }

    /**
     * If word does not appear to be an short form of an abbreviation nor a section marker, return word as is wrapped
     * in an Optional, if it uses German characters.
     * If word is a short form of an abbreviation, expand it to its long form.
     * If word is a section marker, mark is to be filtered by returning Optional.absent();
     *
     * @param word
     * @return an Optional of the unchanged word, or the expanded form or an empty Optional if the word should be filtered
     */

    private Optional<String> expandOrFilterToken(String word) {

        String wordTrimmed = word.trim();

        if (wordTrimmed.endsWith(".")) {
            Optional<String> abbExp = expandAbbreviation(wordTrimmed.substring(0, wordTrimmed.length() - 1));

            if (abbExp.isPresent() && abbExp.get().length() > 0) {
                //TODO: long forms of abbreviations are phrases themselves that possibly should be tokenized...
                return abbExp;
            }
        } else if (SECTION_MARKER_PATTERN.matcher(wordTrimmed).matches()) {
            return Optional.absent();
        }

        return GERMAN_WORD_PATTERN.matcher(wordTrimmed).matches() ? Optional.of(wordTrimmed) : Optional.<String>absent();
    }

    public static String implodeArray(String[] inputArray, String glueString) {
        String output = "";

		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);

			for (int i = 1; i < inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}

			output = sb.toString();
		}

		return output;
	}
}

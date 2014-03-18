package de.fusionfactory.index_vivus.services;

import java.util.Map;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface IAbbreviationSetsService {

    /**
     * Retrieve a map of known abbreviation to their expansions for a specific language or for
     * {@link de.fusionfactory.index_vivus.services.Language#ALL}
     *
     *
     * @param language
     * @return {@link java.util.Map} with abbreviations a keys and their expansions/explanations as values
     */
    Map<String, String> getAbbreviationExpansions(Language language);
}

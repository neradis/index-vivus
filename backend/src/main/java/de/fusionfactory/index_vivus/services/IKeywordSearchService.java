package de.fusionfactory.index_vivus.services;

import de.fusionfactory.index_vivus.models.IDictionaryEntry;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:07
 */
public interface IKeywordSearchService {

    List<? extends IDictionaryEntry> getMatches(String keyword, Language language);

    List<? extends IDictionaryEntry> getMatchesWithAlternative(String keywordCandidate,
                                                               String completionAlternative,
                                                               Language language);

    List<String> getCompletions(String keyword, Language language);
}

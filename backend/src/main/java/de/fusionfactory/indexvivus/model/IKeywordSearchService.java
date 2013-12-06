package de.fusionfactory.indexvivus.model;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:07
 */
public interface IKeywordSearchService {
    List<DictionaryEntry> getMatches(String keyword);
    List<String> getCompletions(String keyword);
}

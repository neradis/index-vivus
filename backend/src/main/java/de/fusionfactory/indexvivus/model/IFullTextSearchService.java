package de.fusionfactory.indexvivus.model;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:10
 */
public interface IFullTextSearchService {
    List<DictionaryEntry> getMatches(String keyword);
}

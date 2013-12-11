package de.fusionfactory.index_vivus.services;

import de.fusionfactory.index_vivus.models.IDictionaryEntry;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:10
 */
public interface IFullTextSearchService {
    List<? extends IDictionaryEntry> getMatches(String keyword);
}

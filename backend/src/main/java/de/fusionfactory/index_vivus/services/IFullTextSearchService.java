package de.fusionfactory.index_vivus.services;

import de.fusionfactory.index_vivus.models.IDictionaryEntry;

import java.util.List;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:10
 */
public interface IFullTextSearchService {

	List<? extends IDictionaryEntry> getMatches(String query, int page, int entries) throws FulltextSearchException;

	List<? extends IDictionaryEntry> getMatches(String query, Language language, int page, int entries) throws FulltextSearchException;
}

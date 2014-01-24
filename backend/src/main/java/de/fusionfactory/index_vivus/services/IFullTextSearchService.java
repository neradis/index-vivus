package de.fusionfactory.index_vivus.services;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 06.12.13
 * Time: 15:10
 */
public interface IFullTextSearchService {

	FulltextResultPage getMatches(String query, int page, int limit) throws FulltextSearchException;

	FulltextResultPage getMatches(String query, Language language, int page, int limit) throws FulltextSearchException;
}

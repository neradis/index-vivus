package de.fusionfactory.index_vivus.services;

/**
 * The search form for full text search in entries will allow to create a number of search files per query.
 * Each search field will either be a description for entries that should be contained in  search results
 * ({@code INCLUDE})or a description for entries that should not appear in the search results ({@code EXCLUDE})
 *
 * <p>Created by Markus Ackermann.
 * No rights reserved.
 */
public enum FieldCondition {
    INCLUDES, EXCLUDES
}

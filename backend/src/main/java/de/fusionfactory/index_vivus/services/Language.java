package de.fusionfactory.index_vivus.services;

/**
 * The possible languages of tokens in the dictionary entries
 *
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public enum Language {
    ALL, /*language ALL will not be used in the chunking, analysis and indexing processes, but it might be sound for
           query request from the front end*/
    GERMAN,
    LATIN,
    GREEK,
    NONE /*language NONE will not be used for formulating queries, but it will make sense in the preprocessing pipeline
         in the backend*/
}

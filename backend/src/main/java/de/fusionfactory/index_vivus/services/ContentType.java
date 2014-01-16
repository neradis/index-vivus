package de.fusionfactory.index_vivus.services;

/**
 * Dictionary entries are composed of numerous constituent subtexts (chunks) that give information on different
 * aspects of lexicographic knowledge. Elements of this enumeration denote some broad categories for such knowledge
 * aspects.
 *
 * <p> Created by Markus Ackermann.
 * No rights reserved.
 */
public enum ContentType {
    MEANING,
    USAGE_EXAMPLE,
    MORPHOLOGY_GRAMMAR,
    REMARK,
    KEYWORD, //at a first glance this content type might appear superficial due to the separate quick keyword query,
             //enabling it as distinct content type for extended full text searches will however enable to query
             //for example for a word with 'spielen' in its description that is not 'ludere'
    OTHER
}

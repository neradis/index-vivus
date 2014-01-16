package de.fusionfactory.index_vivus.services;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface IChunkedEntryProvider {

    /**
     *  A representation of the text content of an entry, rearranged in a manner more convenient for insertion into
     *  full text indices. Before, the following preprocessing was applied to the entry text:
     *  <ul>
     *      <li> chunking of entry text by (language, content-type) categories
     *      <li> removal of extranous whitespace and non-word characters (puntuation, hyphens, etc.)
     *  </ul>
     *
     *  <p> Each entry of this map represents the collection of all chunks belonging to the same category defined by the
     *  2-tupel (language, content-type).
     */
    public static class ChunkedEntry extends HashMap<ChunkType, List<String>> {

        int id;

        public int getId() {
            return id;
        }
    }

    public static class ChunkType {    //TODO: add equals and hashcode to make it Hash*-collection-compatible

        private  final Language language;
        private final Optional<ContentType> contentType;

        public ChunkType(Language language, Optional<ContentType> contentType) {
            this.language = language;
            this.contentType = contentType;
        }

        public Language getLanguage() {
            return language;
        }

        public Optional<ContentType> getContentType() {
            return contentType;
        }
    }

    Iterator<ChunkedEntry> chunkedEntryIterator();
}

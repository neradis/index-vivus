package de.fusionfactory.index_vivus.services;

import com.google.common.base.Optional;

import java.util.LinkedList;

/**
 * Represents a user query against dictionary full text index indices.
 *
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class QueryDescription extends LinkedList<QueryDescription.QueryField> {

    public static class QueryField {  //TODO: add equals and hashcode to make it Hash*-collection-compatible
        private final FieldOptions fieldOptions;
        private final String subQueryString;

        public QueryField(FieldOptions fieldOptions, String subQueryString) {
            this.fieldOptions = fieldOptions;
            this.subQueryString = subQueryString;
        }

        public FieldOptions getFieldOptions() {
            return fieldOptions;
        }

        public String getSubQueryString() {
            return subQueryString;
        }
    }

    public static class FieldOptions { //TODO: add equals and hashcode to make it Hash*-collection-compatible
        private final Optional<Language> language;
        private final Optional<ContentType> contentType;
        private final FieldCondition condition;

        public FieldOptions(Optional<Language> language, Optional<ContentType> contentType, FieldCondition condition) {
           this.language = language;
            this.contentType = contentType;
            this.condition = condition;

        }

        public Optional<Language> getLanguage() {
            return language;
        }

        public Optional<ContentType> getContentType() {
            return contentType;
        }

        public FieldCondition getCondition() {
            return condition;
        }
    }
}

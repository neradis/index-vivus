package de.fusionfactory.index_vivus.language_lookup.methods;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 16:40
 */
public class WiktionaryResponseJson {
    public Query query;

    public class Query {
        HashMap<String, Page> pages;
    }

    public class Page {
        public int pageid;
        public int ns;
        public String title;
        public Category[] categories;
    }

    public class Category {
        public int ns;
        public String title;
    }
}


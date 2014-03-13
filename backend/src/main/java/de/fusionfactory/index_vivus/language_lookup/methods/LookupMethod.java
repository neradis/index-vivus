package de.fusionfactory.index_vivus.language_lookup.methods;

import de.fusionfactory.index_vivus.language_lookup.WordNotFoundException;
import de.fusionfactory.index_vivus.services.Language;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 14:49
 */
public abstract class LookupMethod {
    protected Language _language;

    public LookupMethod(Language expectedLanguage) {
        _language = expectedLanguage;
    }

    public abstract boolean isExpectedLanguage(String word);

    public abstract Language getLanguage(String word) throws WordNotFoundException;
}

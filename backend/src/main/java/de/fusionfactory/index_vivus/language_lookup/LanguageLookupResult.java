package de.fusionfactory.index_vivus.language_lookup;

import de.fusionfactory.index_vivus.services.Language;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 22.01.14
 * Time: 10:07
 */
public class LanguageLookupResult {

    public String word;
    public String dataProvider;
    public Boolean matchedLanguage;
    public Language language;

    public LanguageLookupResult() {
        this("", "", false, null);
    }

    public LanguageLookupResult(String word, String dataProvider, Boolean matchedLanguage, Language language) {
        this.word = word;
        this.dataProvider = dataProvider;
        this.matchedLanguage = matchedLanguage;
        this.language = language;
    }
}

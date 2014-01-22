package de.fusionfactory.index_vivus.language_lookup;

import de.fusionfactory.index_vivus.services.Language;
/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 22.01.14
 * Time: 10:07
 */
public class LanguageLookupResult {

	public String Word;
	public String DataProvider;
	public Boolean MatchedLanguage;
	public Language Language;

	public LanguageLookupResult() {
		Word = "";
		DataProvider = "";
		MatchedLanguage = false;
		Language = null;
	}

	public LanguageLookupResult(String word, String dataProvider, Boolean matchedLanguage, Language language) {
		Word = word;
		DataProvider = dataProvider;
		MatchedLanguage = matchedLanguage;
		Language = language;
	}
}

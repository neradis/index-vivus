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

	public String Word;
	public String DataProvider;
	public Boolean MatchedLanguage;
	public Language Language;
	public ArrayList<LanguageLookupResult> Children;

	public LanguageLookupResult() {
		this("", "", false, null);
	}

	public LanguageLookupResult(String word, String dataProvider, Boolean matchedLanguage, Language language) {
		Word = word;
		DataProvider = dataProvider;
		MatchedLanguage = matchedLanguage;
		Language = language;
		Children = new ArrayList<LanguageLookupResult>();
	}
}

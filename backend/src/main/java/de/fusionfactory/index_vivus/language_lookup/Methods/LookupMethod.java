package de.fusionfactory.index_vivus.language_lookup.Methods;

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

	public abstract boolean IsExpectedLanguage(String word);
	public abstract Language GetLanguage(String word) throws WordNotFoundException;
}

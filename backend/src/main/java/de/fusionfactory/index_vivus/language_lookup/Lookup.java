package de.fusionfactory.index_vivus.language_lookup;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.language_lookup.Methods.LookupMethod;
import de.fusionfactory.index_vivus.language_lookup.Methods.WiktionaryLookup;
import de.fusionfactory.index_vivus.language_lookup.Methods.WordlistLookup;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 14:25
 */
public class Lookup extends LookupMethod {
	private static final List<LookupMethod> _lookupMethods = new ArrayList<LookupMethod>();
	private ArrayList<LanguageLookupResult> _isExpectedLanguage = new ArrayList<LanguageLookupResult>();
	private static Logger logger = Logger.getLogger(Lookup.class);
	private GermanTokenMemory germanTokenMemory;

	public Lookup(Language expectedLanguage) {
		super(expectedLanguage);
		_lookupMethods.addAll(Arrays.asList(
				(LookupMethod) new WordlistLookup(_language),
				(LookupMethod) new WiktionaryLookup(_language)));

		germanTokenMemory = GermanTokenMemory.getInstance();
	}

	@Override
	public boolean IsExpectedLanguage(final String word) {
		if (germanTokenMemory.hasResult(word)) {
			logger.info(word + " found in cache.");
			Optional<Boolean> ret = germanTokenMemory.isGerman(word);
			return (ret.isPresent() && ret.get());
		}

		Thread[] threads = new Thread[_lookupMethods.size()];
		for (int i = 0; i < _lookupMethods.size(); i++) {
			final int finalI = i;
			threads[i] = new Thread() {
				@Override
				public void run() {
					synchronized (_isExpectedLanguage) {
						_isExpectedLanguage.add(new LanguageLookupResult(word,
								parseClassPathToName(_lookupMethods.get(finalI).getClass().getCanonicalName()),
								_lookupMethods.get(finalI).IsExpectedLanguage(word),
								_language));
					}
				}
			};
			threads[i].start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		boolean ret = false;
		for (LanguageLookupResult r : _isExpectedLanguage) {
			logger.info(r.DataProvider + " [" + r.Word + "]: " + r.MatchedLanguage);
			if (r.MatchedLanguage)
				ret = true;
		}

		germanTokenMemory.put(word, ret);

		return ret;
	}

	private static String parseClassPathToName(String classPath) {
		return classPath.substring(classPath.lastIndexOf(".") + 1);
	}

	@Override
	public Language GetLanguage(String word) throws WordNotFoundException {
		return null;
	}
}

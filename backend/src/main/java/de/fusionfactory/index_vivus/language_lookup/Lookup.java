package de.fusionfactory.index_vivus.language_lookup;

import de.fusionfactory.index_vivus.language_lookup.methods.*;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 14:25
 */
public class Lookup extends LookupMethod {
	private static final List<LookupMethod> _lookupMethods = new ArrayList<LookupMethod>();
	private static Logger logger = Logger.getLogger(Lookup.class);
	private GermanTokenMemory germanTokenMemory;
	private static int maxBatchThreads = 10;
	ExecutorService executorService;

	/**
	 * Sets the Max Threads to process the batch request.
	 *
	 * @param i
	 */
	public static void setMaxBatchThreads(int i) {
		if (i < 1)
			throw new IllegalArgumentException("need at least 1 thread for the lookup workers");
		maxBatchThreads = i;
	}

	public Lookup(Language expectedLanguage) {
		super(expectedLanguage);
		_lookupMethods.addAll(Arrays.asList(
				new WordlistLookup(_language),
//				new WiktionaryLookup(_language),
				new StemmedWordListLookup(_language)
				, new UniLeStemmedWordListLookup(_language)
		));

		germanTokenMemory = GermanTokenMemory.getInstance();
		executorService = Executors.newFixedThreadPool(maxBatchThreads);
	}

	/**
	 * Calls an Batch language Check of given Wordlist.
	 *
	 * @param wordList
	 * @return
	 * @throws InterruptedException
	 */
	public List<LanguageLookupResult> isExpectedLanguageBatch(List<String> wordList) throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(wordList.size());

		List<LanguageLookupResult> _isExpectedLanguage = Collections.synchronizedList(new ArrayList<LanguageLookupResult>());

		for (String word : wordList) {
			executorService.execute(new BatchThreadHandler(word, countDownLatch, this, _isExpectedLanguage));
		}
		countDownLatch.await();
//		executorService.shutdown();

		return _isExpectedLanguage;
	}

	/**
	 * Returns an List which contains only Words in given Language.
	 *
	 * @param listWords
	 * @return
	 * @throws InterruptedException
	 */
	public ArrayList<String> getListOfLanguageWords(List<String> listWords) throws InterruptedException {
		List<LanguageLookupResult> list = isExpectedLanguageBatch(listWords);
		ArrayList<String> result = new ArrayList<String>();

		for (LanguageLookupResult r : list) {
			if (r.matchedLanguage)
				result.add(r.word);
		}

		return result;
	}

	@Override
	public boolean isExpectedLanguage(final String word) {
		final ArrayList<LanguageLookupResult> _isExpectedLanguage = new ArrayList<LanguageLookupResult>();

		for (int i = 0; i < _lookupMethods.size(); i++) {
			_isExpectedLanguage.add(new LanguageLookupResult(word,
					parseClassPathToName(_lookupMethods.get(i).getClass().getCanonicalName()),
					_lookupMethods.get(i).isExpectedLanguage(word),
					_language));
		}

		boolean ret = false;
		for (LanguageLookupResult r : _isExpectedLanguage) {
			logger.trace(r.dataProvider + " [" + r.word + "]: " + r.matchedLanguage);
			if (r.matchedLanguage)
				ret = true;
		}

//		germanTokenMemory.put(word, ret);

		return ret;
	}

	public static String parseClassPathToName(String classPath) {
		return classPath.substring(classPath.lastIndexOf(".") + 1);
	}

	@Override
	public Language getLanguage(String word) throws WordNotFoundException {
		return null;
	}

	/**
	 * THe Batch Thread Handler.
	 */
	private class BatchThreadHandler implements Runnable {
		private final String word;
		private final CountDownLatch latch;
		private final Lookup lookup;
		private final List<LanguageLookupResult> languageLookupResults;

		public BatchThreadHandler(String word, CountDownLatch latch, Lookup lookup, List<LanguageLookupResult> languageLookupResults) {
			this.word = word;
			this.latch = latch;
			this.lookup = lookup;
			this.languageLookupResults = languageLookupResults;
		}

		@Override
		public void run() {
			if (word.length() > 0) {
				boolean res = lookup.isExpectedLanguage(word);
//				logger.info("Lookup: " + word);
				languageLookupResults.add(new LanguageLookupResult(word, Lookup.parseClassPathToName(Lookup.class.getCanonicalName()), res, lookup._language));
//				logger.info("Done .. >_< Lookup: " + word);
			}
			latch.countDown();

		}
	}
}

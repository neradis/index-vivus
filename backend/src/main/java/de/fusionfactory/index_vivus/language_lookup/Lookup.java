package de.fusionfactory.index_vivus.language_lookup;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.language_lookup.Methods.*;
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
	private static int MAX_BATCH_THREADS = 10;
	private ExecutorService executorService;
	/**
	 * Sets the Max Threads to process the batch request.
	 *
	 * @param i
	 */
	public static void SetMaxBatchThreads(int i) {
		if (i < 1)
			return;
		MAX_BATCH_THREADS = i;
	}

	public Lookup(Language expectedLanguage) {
		super(expectedLanguage);
		executorService = Executors.newFixedThreadPool(MAX_BATCH_THREADS);
		_lookupMethods.addAll(Arrays.asList(
				new WordlistLookup(_language),
//				new WiktionaryLookup(_language),
				new StemmedWordListLookup(_language)
				, new UniLeStemmedWordListLookup(_language)
		));

		germanTokenMemory = GermanTokenMemory.getInstance();
	}

	/**
	 * Calls an Batch Language Check of given Wordlist.
	 *
	 * @param listWords
	 * @return
	 * @throws InterruptedException
	 */
	public List<LanguageLookupResult> IsExpectedLanguageBatch(List<String> listWords) throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(listWords.size());

		List<LanguageLookupResult> _isExpectedLanguage = Collections.synchronizedList(new ArrayList<LanguageLookupResult>());

		for (String word : listWords) {
			executorService.execute(new BatchThreadHandler(word, countDownLatch, this, _isExpectedLanguage));
		}
		countDownLatch.await();
		return _isExpectedLanguage;
	}

	/**
	 * Returns an List which contains only Words in given Language.
	 *
	 * @param listWords
	 * @return
	 * @throws InterruptedException
	 */
	public ArrayList<String> GetListOfLanguageWords(List<String> listWords) throws InterruptedException {
		List<LanguageLookupResult> list = IsExpectedLanguageBatch(listWords);
		ArrayList<String> result = new ArrayList<String>();

		for (LanguageLookupResult r : list) {
			if (r.MatchedLanguage)
				result.add(r.Word);
		}

		return result;
	}

	@Override
	public boolean IsExpectedLanguage(final String word) {
//		if (germanTokenMemory.hasResult(word)) {
//			logger.trace(word + " found in cache.");
//			Optional<Boolean> ret = germanTokenMemory.isGerman(word);
//			return (ret.isPresent() && ret.get());
//		}
		final ArrayList<LanguageLookupResult> _isExpectedLanguage = new ArrayList<LanguageLookupResult>();

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
			logger.trace(r.DataProvider + " [" + r.Word + "]: " + r.MatchedLanguage);
			if (r.MatchedLanguage)
				ret = true;
		}

//		germanTokenMemory.put(word, ret);

		return ret;
	}

	public static String parseClassPathToName(String classPath) {
		return classPath.substring(classPath.lastIndexOf(".") + 1);
	}

	@Override
	public Language GetLanguage(String word) throws WordNotFoundException {
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
				boolean res = lookup.IsExpectedLanguage(word);
//				logger.info("Lookup: " + word);
				languageLookupResults.add(new LanguageLookupResult(word, Lookup.parseClassPathToName(Lookup.class.getCanonicalName()), res, lookup._language));
//				logger.info("Done .. >_< Lookup: " + word);
			}
			latch.countDown();
		}
	}
}

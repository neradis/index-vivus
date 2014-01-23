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

	public static void SetMaxBatchThreads(int i) {
		if (i < 1)
			return;
		MAX_BATCH_THREADS = i;
	}

	public Lookup(Language expectedLanguage) {
		super(expectedLanguage);
		_lookupMethods.addAll(Arrays.asList(
				new WordlistLookup(_language),
				new WiktionaryLookup(_language),
				new StemmedWordListLookup(_language),
				new UniLeStemmedWordListLookup(_language)));

		germanTokenMemory = GermanTokenMemory.getInstance();
	}

	public ArrayList<LanguageLookupResult> IsExpectedLanguageBatch(List<String> listWords) throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(listWords.size());
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_BATCH_THREADS);
		ArrayList<LanguageLookupResult> _isExpectedLanguage = new ArrayList<LanguageLookupResult>();

		for (String word : listWords) {
			executorService.execute(new BatchThreadHandler(word, countDownLatch, this, _isExpectedLanguage));
		}
		countDownLatch.await();
		return _isExpectedLanguage;
	}

	@Override
	public boolean IsExpectedLanguage(final String word) {
		if (germanTokenMemory.hasResult(word)) {
			logger.trace(word + " found in cache.");
			Optional<Boolean> ret = germanTokenMemory.isGerman(word);
			return (ret.isPresent() && ret.get());
		}
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
			logger.info(r.DataProvider + " [" + r.Word + "]: " + r.MatchedLanguage);
			if (r.MatchedLanguage)
				ret = true;
		}

		germanTokenMemory.put(word, ret);

		return ret;
	}

	public static String parseClassPathToName(String classPath) {
		return classPath.substring(classPath.lastIndexOf(".") + 1);
	}

	@Override
	public Language GetLanguage(String word) throws WordNotFoundException {
		return null;
	}

	private class BatchThreadHandler implements Runnable {
		private final String word;
		private final CountDownLatch latch;
		private final Lookup lookup;
		private final ArrayList<LanguageLookupResult> languageLookupResults;

		public BatchThreadHandler(String word, CountDownLatch latch, Lookup lookup, ArrayList<LanguageLookupResult> languageLookupResults) {
			this.word = word;
			this.latch = latch;
			this.lookup = lookup;
			this.languageLookupResults = languageLookupResults;
		}

		@Override
		public void run() {
			boolean res = lookup.IsExpectedLanguage(word);
			languageLookupResults.add(new LanguageLookupResult(word, Lookup.parseClassPathToName(Lookup.class.getCanonicalName()), res, lookup._language));
			latch.countDown();
		}
	}
}

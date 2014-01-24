package de.fusionfactory.index_vivus.language_lookup;

import com.google.common.io.Resources;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 15:15
 */
public class main {

	public ArrayList<String> _wordList = new ArrayList<String>();
	private static String wordListFile = Resources.getResource("word_language/top10000de.txt").getPath();
	private Logger logger;

	private void testOurWordListFile() {
		Lookup lookup = new Lookup(Language.GERMAN);
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();

		try {
			BufferedReader br = new BufferedReader(new FileReader(wordListFile));
			String line = br.readLine();
			while (line != null) {
				_wordList.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();

		logger.info("Build WordList in " + ((endTime - startTime) / 1000) + " seconds.");

		try {
			startTime = System.currentTimeMillis();
			List<LanguageLookupResult> resultArrayList = lookup.IsExpectedLanguageBatch(_wordList);
			endTime = System.currentTimeMillis();
			logger.info("Check WordList with " + _wordList.size() + " Elements in " + ((endTime - startTime) / 1000) + " seconds.");

			int notMatched = 0;
			for (LanguageLookupResult result : resultArrayList) {
				if (!result.MatchedLanguage) {
					notMatched++;
				}
			}

			logger.info(notMatched + " Words are not Wiktionary-German tagged.");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void userInput() {
		InputStreamReader isReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(isReader);
		Lookup lookup = new Lookup(Language.GERMAN);

		while (true) {
			System.out.print("\n> ");
			System.out.flush();
			try {
				String keyword = bufferedReader.readLine();
				System.out.println(keyword + ": " + lookup.IsExpectedLanguage(keyword));
			} catch (IOException e) {
				break;
			}
		}
	}

	private void testBatchRequest() {
		ArrayList<String> requestedWordList = new ArrayList<>();
		requestedWordList.addAll(Arrays.asList(
				"machen", "schwimme", "call", "mouse", "maus", "was", "geht", "ab"
		));
		Lookup lookup = new Lookup(Language.GERMAN);
		try {
			ArrayList<String> result = lookup.GetListOfLanguageWords(requestedWordList);
			for (String s : result) {
				logger.info(s);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public main(String[] args) {
		logger = Logger.getLogger(main.class);
		DbHelper.createMissingTables();

		if (args.length == 0) {
			userInput();
			return;
		} else if (args[0].equals("wordListCheck")) {
			testOurWordListFile();
		} else if (args[0].equals("batchRequest")) {
			testBatchRequest();
		}
	}

	public static void main(String[] args) {
		new main(args);
	}
}
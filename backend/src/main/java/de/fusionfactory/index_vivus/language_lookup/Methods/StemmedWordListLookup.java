package de.fusionfactory.index_vivus.language_lookup.Methods;

import com.google.common.io.Resources;
import de.fusionfactory.index_vivus.language_lookup.GermanStemmer;
import de.fusionfactory.index_vivus.language_lookup.WordNotFoundException;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 11:27
 */
public class StemmedWordListLookup extends LookupMethod {
	private static String wordListFile = Resources.getResource("word_language/top10000de.txt").getPath();
	private Logger logger = Logger.getLogger(StemmedWordListLookup.class);
	private HashSet<String> hashSet = new HashSet<String>();

	public StemmedWordListLookup(Language expectedLanguage) {
		super(expectedLanguage);
		if (!expectedLanguage.equals(Language.GERMAN)) {
			throw new RuntimeException("This Lookup method only supports german language.");
		}

		generateStemmedWordlist();
	}

	@Override
	public boolean IsExpectedLanguage(String word) {
		word = word.toLowerCase().trim();
		GermanStemmer stemmer = new GermanStemmer();
		return hashSet.contains(stemmer.getStemmed(word));
	}

	@Override
	public Language GetLanguage(String word) throws WordNotFoundException {
		return null;
	}

	/**
	 * generates a stemmed wordlist from our german top10.000 wordlist
	 */
	private void generateStemmedWordlist() {
		logger.info("Collect stemmed words to HashSet.");
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(wordListFile)));
			GermanStemmer stemmer = new GermanStemmer();

			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().toLowerCase();
				hashSet.add(stemmer.getStemmed(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		logger.info("Collect stemmed wordlist HashSet done..");
	}
}

package de.fusionfactory.index_vivus.language_lookup.Methods;

import com.google.common.io.Resources;
import de.fusionfactory.index_vivus.language_lookup.WordNotFoundException;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 14:58
 */
public class WordlistLookup extends LookupMethod {
	private static String wordListFile = Resources.getResource("word_language/top10000de.txt").getPath();
	private Logger logger = Logger.getLogger(WordlistLookup.class);
	private HashSet<String> hashSet;

	public WordlistLookup(Language l) {
		super(l);
		if (!l.equals(Language.GERMAN)) {
			throw new RuntimeException("This Lookup method only supports german language.");
		}

		hashSet = new HashSet<>();
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(wordListFile)));
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().toLowerCase();
				hashSet.add(line.toLowerCase());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean IsExpectedLanguage(String word) {
		return hashSet.contains(word.toLowerCase().trim());
	}

	@Override
	public Language GetLanguage(String word) throws WordNotFoundException {
		if (IsExpectedLanguage(word))
			return Language.GERMAN;

		throw new WordNotFoundException(word + " not found in wordlist.");
	}
}

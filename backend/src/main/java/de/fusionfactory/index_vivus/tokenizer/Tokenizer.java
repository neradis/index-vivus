package de.fusionfactory.index_vivus.tokenizer;

import de.fusionfactory.index_vivus.language_lookup.Lookup;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 13:50
 */
public class Tokenizer {
	private List<Abbreviation> abbr;
	private Lookup lookup;
	private Logger logger;

	public Tokenizer() {
		abbr = Abbreviation.fetchAll();
		lookup = new Lookup(Language.GERMAN);
		logger = Logger.getLogger(this.getClass());
	}

	public List<String> getTokenizedString(String entry) {
		ArrayList<String> result = new ArrayList<String>();

		String[] lines = entry.split("\n");

		for (int i = 0; i < lines.length; i++) {
			result.addAll(getTokenizedLine(lines[i]));
		}


		return result;
	}

	private String getAbbreviation(String word) {
		for (Abbreviation a : abbr) {
			if (a.shortForm().equals(word)) {
				return a.longForm();
			}
		}

		return null;
	}

	private List<String> getTokenizedLine(String line) {
		ArrayList<String> result = new ArrayList<>();

		if (line.length() < 1) {
			return result;
		}

		String[] words = line.split(" ");

		for (int i = 0; i < words.length; i++) {

			// could be an abbreviation.
			int dotIndex = words[i].indexOf('.');
			if (dotIndex >= 0) {
				String longForm = getAbbreviation(words[i].substring(0, dotIndex + 1));
				if (longForm != null && longForm.length() > 0) {
					words[i] = longForm;
				}
			}
			// String contains 'römische zahl)' and should be removed from our list.
			else if (words[i].matches("[IVX]{1,}\\)[\\W]{0,}")) {
				words[i] = "";
			}
			// matches A) or B) or C) and remove it.
			else if (words[i].matches("[ABC]{1}\\)[\\W]{0,}")) {
				words[i] = "";
			}
		}
		line = implodeArray(words, " ");
		line = line.replaceAll("[^0-9a-zA-ZäöüÄÖÜ]", " ");
		words = line.split(" ");

		try {
			List<String> list = lookup.GetListOfLanguageWords(Arrays.asList(words));
			for (String l : list) {
				if (l.length() > 1)
					result.add(l);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String implodeArray(String[] inputArray, String glueString) {
		String output = "";

		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);

			for (int i = 1; i < inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}

			output = sb.toString();
		}

		return output;
	}
}

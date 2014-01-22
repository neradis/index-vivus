package de.fusionfactory.index_vivus.language_lookup;

import de.fusionfactory.index_vivus.language_lookup.Methods.LookupMethod;
import de.fusionfactory.index_vivus.language_lookup.Methods.WiktionaryLookup;
import de.fusionfactory.index_vivus.language_lookup.Methods.WordlistLookup;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 15:15
 */
public class main {
	public main() {
		InputStreamReader isReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(isReader);
		Lookup lookup = new Lookup(Language.GERMAN);
		DbHelper.createMissingTables();

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



	public static void main(String[] args) {
		new main();
	}
}

package de.fusionfactory.index_vivus.language_lookup;

import de.fusionfactory.index_vivus.language_lookup.Methods.LookupMethod;
import de.fusionfactory.index_vivus.language_lookup.Methods.WiktionaryLookup;
import de.fusionfactory.index_vivus.language_lookup.Methods.WordlistLookup;

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
	private static final List<LookupMethod> _lookupMethods = Arrays.asList(
			(LookupMethod) new WordlistLookup(Language.GERMAN),
			(LookupMethod)new WiktionaryLookup(Language.GERMAN));

	public main() {
		InputStreamReader isReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(isReader);

		while (true) {
			System.out.print("\n> ");
			System.out.flush();

			try {
				String keyword = bufferedReader.readLine();

				for (LookupMethod lookup : _lookupMethods) {
					System.out.println(

							parseClassPathToName(lookup.getClass().getCanonicalName())
									+ ": " + lookup.IsExpectedLanguage(keyword)

					);
				}

			} catch (IOException e) {
				break;
			}
		}
	}

	private static String parseClassPathToName(String classPath) {
		return classPath.substring(classPath.lastIndexOf(".") + 1);
	}

	public static void main(String[] args) {
		new main();
	}
}

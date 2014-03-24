package de.fusionfactory.index_vivus.language_lookup.methods;

import de.fusionfactory.index_vivus.language_lookup.GermanStemmer;
import de.fusionfactory.index_vivus.language_lookup.WordNotFoundException;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.tools.scala.Utils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
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
  private static File wordListFile = Utils.fileForResouce("word_language/top10000de.txt");
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
    public boolean isExpectedLanguage(String word) {
        word = word.toLowerCase().trim();
        GermanStemmer stemmer = new GermanStemmer();
        return hashSet.contains(stemmer.getStemmed(word));
    }

    @Override
    public Language getLanguage(String word) throws WordNotFoundException {
        return null;
    }

    /**
     * generates a stemmed wordlist from our german top10.000 wordlist
     */
    private void generateStemmedWordlist() {
        logger.info("Write stemmed words to HashSet.");
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

        logger.info("Write stemmed wordlist HashSet done..");
    }
}

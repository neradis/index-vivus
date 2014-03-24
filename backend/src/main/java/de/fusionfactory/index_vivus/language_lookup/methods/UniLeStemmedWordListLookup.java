package de.fusionfactory.index_vivus.language_lookup.methods;

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
 * User: Eric Kurzhals@
 * Date: 23.01.14
 * Time: 11:40
 */
public class UniLeStemmedWordListLookup extends LookupMethod {
  private static File wordListFile = Utils.fileForResouce("word_language/word_baseform_minimized.txt");
    private Logger logger = Logger.getLogger(this.getClass());
    private HashSet<String> hashSet = new HashSet<String>();

    public UniLeStemmedWordListLookup(Language expectedLanguage) {
        super(expectedLanguage);
        if (!expectedLanguage.equals(Language.GERMAN)) {
            throw new RuntimeException("This Lookup method only supports german language.");
        }

        generateWordListHashSet();
    }

    private void generateWordListHashSet() {
        logger.info("Write Wordlist to HashSet.");

        try {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(wordListFile)));
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().toLowerCase();
                String[] tokens = line.split("\t");
                for (String t : tokens) {
                    hashSet.add(t.trim());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        logger.info("HashSet size: " + hashSet.size());

        logger.info("Done writing Wordlist to HashSet.");
    }

    @Override
    public boolean isExpectedLanguage(String word) {
        word = word.toLowerCase().trim();
        return hashSet.contains(word);
    }

    @Override
    public Language getLanguage(String word) throws WordNotFoundException {
        return null;

    }
}

package de.fusionfactory.index_vivus.spellchecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 02.12.13
 * Time: 20:48
 */
public class main {

    public static void main(String[] args) {
        System.out.println("test");
        SpellChecker sc = new SpellChecker();
        System.out.println("Convert our Wordlist to the index");
        try {
            sc.createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);

        while (true) {
            System.out.print("\n> ");
            System.out.flush();
            String keyword = null;
            try {
                keyword = bufReader.readLine();
                if (keyword.length() == 0)
                    break;

                String[] suggestions = sc.getAutocompleteSuggestions(keyword);
                for (int i = 0; i < suggestions.length; i++) {
                    System.out.println("#" + (i + 1) + ": " + suggestions[i]);
                }
                String alternative = sc.getAlternativeWords(keyword);
                System.out.println("found alternative: " + alternative);

            } catch (IOException e) {
                break;
            }
            catch (SpellCheckerException e) {
                break;
            }
        }
    }
}
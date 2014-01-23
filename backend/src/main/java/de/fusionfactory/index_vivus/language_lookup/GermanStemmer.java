package de.fusionfactory.index_vivus.language_lookup;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 11:45
 */
public class GermanStemmer extends org.apache.lucene.analysis.de.GermanStemmer {
	public String getStemmed(String term) {
		return stem(term);
	}
}

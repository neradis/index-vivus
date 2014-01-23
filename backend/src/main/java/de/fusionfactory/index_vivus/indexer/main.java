package de.fusionfactory.index_vivus.indexer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:46
 */
public class main {
	public main(String[] args) {
		Indexer indexer = new Indexer();

		try {
			indexer.mapIndexToRam();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new main(args);
	}
}

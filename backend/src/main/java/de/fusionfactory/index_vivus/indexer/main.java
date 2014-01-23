package de.fusionfactory.index_vivus.indexer;

import de.fusionfactory.index_vivus.xmlimport.GeorgesImporter;
import de.fusionfactory.index_vivus.xmlimport.Importer;
import org.xml.sax.SAXException;

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
//			Importer xmlImporter = new GeorgesImporter();
//			xmlImporter.importFromDefaultLocation();

			indexer.mapIndexToRam();

		} catch (IOException e) {
			e.printStackTrace();
		}
//		catch (SAXException e) {
//			e.printStackTrace();
//		}
	}

	public static void main(String[] args) {
		new main(args);
	}
}

package de.fusionfactory.index_vivus.indexer;

import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:46
 */
public class main {
    public main(String[] args) {
        Indexer indexer = new Indexer();
        Logger logger = Logger.getLogger(main.class);
        try {
//			Importer xmlImporter = new GeorgesImporter();
//			xmlImporter.importFromDefaultLocation();

            indexer.ensureIndexCreated();
            try {
                List<DictionaryEntry> entries = indexer.getTopSearchResults("buchstabe");
                for (DictionaryEntry e : entries) {
                    logger.info("Found:" + e.getKeyword());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

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

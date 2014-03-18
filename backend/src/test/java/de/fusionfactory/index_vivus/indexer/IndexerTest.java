package de.fusionfactory.index_vivus.indexer;

import de.fusionfactory.index_vivus.models.IDictionaryEntry;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCount;
import de.fusionfactory.index_vivus.xmlimport.GeorgesImporter;
import de.fusionfactory.index_vivus.xmlimport.Importer;
import org.apache.log4j.Logger;
import org.testng.annotations.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * Author: Eric Kurzhals <kurzhals@studserv.uni-leipzig.de>
 * Date: 18.03.14
 * Time: 11:32
 */
public class IndexerTest {
    Indexer indexer = null;
    Logger logger;

    @BeforeMethod
    public void setUp() {
        Importer xmlImporter = new GeorgesImporter();

        logger = Logger.getLogger(IndexerTest.class);
        indexer = new Indexer();

        try {
            xmlImporter.importFromDefaultLocation();


            indexer.ensureIndexCreated();
            logger.info("Build up index.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        final String query = "buchstabe";

        List<DictionaryEntry> result = indexer.getTopSearchResults(query, Language.ALL);

        logger.info(result.size());
        assert(result.size() > 0);

    }
}

package de.fusionfactory.index_vivus.indexer;

import de.fusionfactory.index_vivus.models.IDictionaryEntry;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCount;
import de.fusionfactory.index_vivus.xmlimport.GeorgesImporter;
import de.fusionfactory.index_vivus.xmlimport.Importer;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Eric Kurzhals <kurzhals@studserv.uni-leipzig.de>
 * Date: 18.03.14
 * Time: 11:32
 */
public class IndexerTest {
    Indexer indexer = null;
    Logger logger;

    @BeforeClass
    public void setUp() {
//        Importer xmlImporter = new GeorgesImporter();
//
        logger = Logger.getLogger(IndexerTest.class);
        indexer = new Indexer();

        try {
//            xmlImporter.importFromDefaultLocation();


//            indexer.ensureIndexCreated();
            logger.info("Build up index.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOR() {
        final String query = "buchstabe or anfangsbuchstaben";
        final int expectedIds[] = {1, 654};

        List<DictionaryEntry> result = indexer.getTopSearchResults(query, Language.ALL);
        List<Integer> ids = new ArrayList<>();
        for (DictionaryEntry e : result) {
            ids.add(e.getId());
        }

        Assert.assertEquals(ids.size(), expectedIds.length);

        for(int i : expectedIds) {
            Assert.assertTrue(ids.contains(new Integer(i)));
        }
    }

    @Test
    public void testANDnothing() {
        final String query = "buchstabe AND anfangsbuchstaben";
        final int expectedIds[] = {};

        List<DictionaryEntry> result = indexer.getTopSearchResults(query, Language.ALL);
        List<Integer> ids = new ArrayList<>();
        for (DictionaryEntry e : result) {
            ids.add(e.getId());
        }

        Assert.assertEquals(ids.size(), expectedIds.length);

        for(int i : expectedIds) {
            Assert.assertTrue(ids.contains(new Integer(i)));
        }
    }

    @Test
    public void testAND() {
        final String query = "buchstabe AND vorname";
        final int expectedId = 1;
        final int excludedId = 654;

        List<DictionaryEntry> result = indexer.getTopSearchResults(query, Language.ALL);
        logger.info("Size: " + result.size());
        List<Integer> ids = new ArrayList<>();
        Assert.assertTrue(result.size() > 0, "result.size() > 0");

        for (DictionaryEntry e : result) {
            logger.info(e.getId());
            Assert.assertTrue(e.getId() != excludedId, "docId is an excluded ID");
            ids.add(e.getId());
        }

        Assert.assertTrue(ids.contains(new Integer(expectedId)));
    }
}

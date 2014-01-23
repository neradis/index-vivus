package de.fusionfactory.index_vivus.testing;

import de.fusionfactory.index_vivus.configuration.SettingsProvider;
import de.fusionfactory.index_vivus.xmlimport.GeorgesImporter;
import de.fusionfactory.index_vivus.xmlimport.Importer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 23:13
 */
public class ImporterTest {
    private static Logger logger = Logger.getLogger(ImporterTest.class);
    public static void main(String []args) {
        Importer imp = new GeorgesImporter();
        //logger.setLevel(Level.DEBUG);
        logger.info("Starting Importer...");
        try {
            logger.debug(SettingsProvider.getInstance().getDatabaseUrl());
            imp.importFromDefaultLocation();
            //imp.importDir("D:\\Eigene Daten\\Projekte\\java\\index-vivus\\backend\\inputs\\test\\dictionaries");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}

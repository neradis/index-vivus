package test;

import de.fusionfactory.index_vivus.xmlimport.Importer;
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
        Importer imp = new Importer();

        logger.info("Starte Importer-Klasse...");
        try {
            imp.importDir("D:\\Temp\\xmlData\\Georges-1913");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}

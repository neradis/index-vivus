package test;

import de.fusionfactory.indexvivus.xmlimport.Importer;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 23:13
 */
public class ImporterTest {
    public static void main(String []args) {
        Importer imp = new Importer();
        try {
            imp.importDir("D:\\Temp\\xmlData\\Georges-1913");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}

package de.fusionfactory.indexvivus.xmlimport;

import de.fusionfactory.indexvivus.model.DictionaryEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 22:55
 */
public class Importer {
    ArrayList<DictionaryEntry> importedEntries = null;

    public Importer() {
        importedEntries = new ArrayList<>();
    }
    private void parseMetaData(File inpFile) {
        throw new UnsupportedOperationException("getRelated is not implemented yet.");
    }
    private void parseEntryData(Node node) throws IOException, SAXException {
        System.out.println(node.getNodeName() + " " + node.getNodeType());
        NodeList subNodes = node.getChildNodes();
        for(int i = 0; i < subNodes.getLength(); i++) {
            this.parseEntryData(subNodes.item(i));
        }
    }

    public void importDir(String fileName) throws IOException, SAXException {
        File dirHandle =new File(fileName);
        File[] files = dirHandle.listFiles();
        if(files != null) {
            for(File fileHandle : files)
                if (fileHandle.isFile() && fileHandle.getName().contains(".xml")) {
                    try {
                        DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
                        docBuildFac.setIgnoringComments(true);
                        DocumentBuilder docBuild = docBuildFac.newDocumentBuilder();
                        Document content = docBuild.parse(fileHandle);
                        content.normalizeDocument();
                        //only 1 the root node "doc"
                        NodeList notes = content.getChildNodes();
                        if (fileHandle.getName().contains("000"))
                            System.out.println("Meta");
                        else {
                           this.parseEntryData(notes.item(0));
                        }
                    } catch (ParserConfigurationException e) { // shouldn't be thrown with default configuration
                        e.printStackTrace();
                    }
                    //first file in a collection starts with 000 and contains meta info


                }
        }
    }
}

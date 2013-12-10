package de.fusionfactory.index_vivus.xmlimport;

import de.fusionfactory.index_vivus.models.DictionaryEntry;
import de.fusionfactory.index_vivus.models.WordType;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
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
    private static Logger logger = Logger.getLogger(Importer.class);

    private ArrayList<DictionaryEntry> importedEntries = null;
    private long currentEntryCounter = 0;
    private DOMImplementationLS currentDomImpl = null;

    public Importer() {
        importedEntries = new ArrayList<>();
    }
    private void parseInnerInformation(Element article) {

    }

    private void parseMetaData(File inpFile) {
        throw new UnsupportedOperationException("getRelated is not implemented yet.");
    }

    private void parseEntryData(NodeList entries) throws IOException, SAXException {
        //foreach entry
        for(int i = 0; i < entries.getLength(); i++) {
            long id = ++this.currentEntryCounter;
            logger.info("Eintrag #" + id + "");
            String keyword = "";
            String description = "";
            WordType wordType = WordType.UNKOWN;
            int keyGroupIndex = 1;
            NodeList entryContent = entries.item(i).getChildNodes();
            for(int c = 0; c < entryContent.getLength(); c ++) {
                Node childContent = entryContent.item(c);
                // keyword
                if(childContent.getNodeName().equals("lem")) {
                    String buf = childContent.getFirstChild().getNodeValue();
                    if(buf.contains("[")) {
                        //keyword without the ordinal number
                        keyword = buf.substring(0,buf.indexOf("[")).trim();
                        //take the number within square brackets
                        keyGroupIndex = Integer.parseInt(buf.substring(buf.indexOf("[") + 1, buf.indexOf("]")));
                    } else
                        keyword = buf.trim();
                }
                // text content containing one or no header element (<h3>) and a <p> element
                else if(childContent.getNodeName().equals("text")) {
                    NodeList descContentNodes = childContent.getChildNodes();
                    for(int d = 0; d < descContentNodes.getLength(); d++) {
                        Node innerChild = descContentNodes.item(d);
                        //one of the description for the entry
                        if(innerChild.getNodeName().equals("p")) {
                            DOMImplementationLS domImplLS = this.currentDomImpl;
                            LSSerializer serializer = domImplLS.createLSSerializer();
                            serializer .getDomConfig().setParameter("xml-declaration", false);
                            description += serializer.writeToString(innerChild) + "\n";
                        }
                    }
                }

            }
            logger.info(keyword + " " + wordType + " " + keyGroupIndex + "\n" + description);
            if(this.currentEntryCounter > 3)
                break;
        }
    }

    public void importDir(String fileName) throws IOException, SAXException {
        File dirHandle =new File(fileName);
        File[] files = dirHandle.listFiles();
        if(files != null) {
            for(File fileHandle : files) {
                if (fileHandle.isFile() && fileHandle.getName().contains(".xml")) {
                    logger.info("Datei: '" + fileHandle.getAbsoluteFile() + "' wird verarbeitet.");
                    try {
                        DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
                        docBuildFac.setIgnoringComments(true);
                        DocumentBuilder docBuild = docBuildFac.newDocumentBuilder();
                        Document content = docBuild.parse(fileHandle);
                        content.normalizeDocument();
                        this.currentDomImpl = (DOMImplementationLS)content.getImplementation();
                        //first file in a collection starts with 000 and contains meta info
                        if (fileHandle.getName().contains("000"))
                            logger.info("Meta");
                        else {
                            // all entries in the file
                            logger.info("Data");
                            NodeList nodes = content.getElementsByTagName("article");
                            this.parseEntryData(nodes);
                        }
                    } catch (ParserConfigurationException e) { // shouldn't be thrown with default configuration
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}

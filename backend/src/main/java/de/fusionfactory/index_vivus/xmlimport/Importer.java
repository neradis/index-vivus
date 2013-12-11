package de.fusionfactory.index_vivus.xmlimport;

import de.fusionfactory.index_vivus.models.WordType;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 22:55
 */
public class Importer {
    private static Logger logger = Logger.getLogger(Importer.class);

    private long currentEntryCounter = 0;

    public Importer() {}

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
                    String buf = childContent.getFirstChild().getNodeValue().trim();
                    //personal name ergo noun
                    if(Character.isUpperCase(buf.charAt(0)))
                        wordType = WordType.NOUN;

                    if(buf.contains("[") && !buf.contains("[*]")) {
                        //keyword without the ordinal number
                        keyword = buf.substring(0,buf.indexOf("[")).trim();
                        //take the number within square brackets
                        keyGroupIndex = Integer.parseInt(buf.substring(buf.indexOf("[") + 1, buf.indexOf("]")));
                    } else if(buf.contains("[*]"))  //@TODO find out what this means for an entry and if information is needed
                        keyword = buf.substring(0,buf.indexOf("[")).trim();
                    else
                        keyword = buf;
                }

                // text content containing one or no header element (<h3>) and a <p> element
                else if(childContent.getNodeName().equals("text")) {
                    NodeList descContentNodes = childContent.getChildNodes();
                    for(int d = 0; d < descContentNodes.getLength(); d++) {
                        Node innerChild = descContentNodes.item(d);
                        //one of the description for the entry
                        if(innerChild.getNodeName().equals("p")) {
                            StringWriter outStr = new StringWriter();
                            Transformer transformer = null;
                            try {
                                transformer = TransformerFactory.newInstance().newTransformer();
                                //ensure utf-8 encoding and no xml header in the resulting string
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                transformer.transform(new DOMSource(innerChild), new StreamResult(outStr));
                            } catch (TransformerException e) {
                                logger.error(e.getStackTrace());
                            }
                            description += outStr.toString();
                        }
                    }
                }

            }
            logger.info(keyword + " " + wordType + " " + keyGroupIndex + "\n" + description);
            //if(this.currentEntryCounter > 3)
            //break;
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

package de.fusionfactory.index_vivus.xmlimport;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.Environment;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import scala.slick.session.Session;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static java.lang.String.format;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 22:55
 */
public class Importer {
    private static Logger logger = Logger.getLogger(Importer.class);
    private final Optional<Integer> integerAbsent = Optional.absent();
    private final Optional<String> stringAbsent = Optional.absent();
    private long currentEntryCounter = 0;

    private boolean debug = false;

    public Importer() {
        if(Environment.getActive().equals(Environment.DEVELOPMENT) || Environment.getActive().equals(Environment.TEST)) {
            debug = true;
            DbHelper.createMissingTables();
        }
    }

    private void parseInnerInformation(Element article) {
    }

    private void parseMetaData(File inpFile) {
        throw new UnsupportedOperationException("getRelated is not implemented yet.");
    }

    private void parseEntryData(NodeList entries) throws IOException, SAXException {
        //foreach entry
        Optional<DictionaryEntry> prevEntry = Optional.absent();
        for(int i = 0; i < entries.getLength(); i++) {
            long id = ++this.currentEntryCounter;
            if(this.currentEntryCounter > 3)
                break;
            //logger.info("Eintrag #" + id + "");
            String keyword = "";
            String description = "";
            String descriptionHtml = null;
            WordType wordType = WordType.UNKNOWN;
            byte keyGroupIndex = 1;
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
                        keyGroupIndex = Byte.parseByte(buf.substring(buf.indexOf("[") + 1, buf.indexOf("]")));
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
                            //WTF java: adding to the word NULL ???
                            if(descriptionHtml == null)
                                descriptionHtml = outStr.toString();
                            else
                                descriptionHtml += outStr.toString();
                        }
                    }
                    //strips all the html tags and unescape resulting string from cleaner
                    description = StringEscapeUtils.unescapeHtml4(Jsoup.clean(descriptionHtml, Whitelist.none()));
                }
            }
            DictionaryEntry currentEntry = DictionaryEntry.create(integerAbsent, integerAbsent, keyGroupIndex, keyword, description, Optional.of(descriptionHtml), wordType);
            ImportTransaction trans = new ImportTransaction(prevEntry,currentEntry);
            logger.info(trans.getPrevE());
            DbHelper.transaction(trans);
            logger.info(trans.getCurrentE());
            prevEntry = trans.getPrevE();


            //IDictionaryEntry currentEntry = ModelFactory.createDictionaryEntry(keyword, description, keyGroupIndex);
            //    currentEntry.setHtmlDescription(descriptionHtml);
            /* TODO: when implemented, activate these setters:
              currentEntry.setWordType(wordType);*/
             //   logger.info(((DictionaryEntry)currentEntry).toString());
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

    public class ImportTransaction extends DbHelper.Operations<Object> {
        private Optional<DictionaryEntry> prevE;
        private DictionaryEntry currentE;

        public ImportTransaction(Optional<DictionaryEntry> prevEntry, DictionaryEntry currentEntry) {
            prevE = prevEntry;
            currentE = currentEntry;
        }

        @Override
        public Object perform(Session tx) {
                List<DictionaryEntry> duplicates = currentE.crud(tx).duplicateList();
                if (duplicates.isEmpty()) { // no duplicate entries!
                    if (prevE.isPresent()) {
                        currentE.setPrevId(Optional.of(prevE.get().getId()));
                    }
                    currentE = currentE.crud(tx).insertAsNew();
                    if (prevE.isPresent()) {
                        prevE.get().setNextId(currentE.getIdOptional());
                        prevE.get().crud(tx).update();
                    }
                    //set current Entry as previous Entry
                    prevE = Optional.of(currentE);
                } else {
                    String dupList = Joiner.on(" \n").join(duplicates);
                    logger.warn(format("Already found entries in database with same content as %s:%n%s%n - SKIPPED",
                            currentE, dupList));
                }
            return null; //nothing to return
        }

        public Optional<DictionaryEntry> getPrevE() {
            return prevE;
        }

        public DictionaryEntry getCurrentE() {
            return currentE;
        }
    }
}

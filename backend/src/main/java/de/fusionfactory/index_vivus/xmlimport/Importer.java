package de.fusionfactory.index_vivus.xmlimport;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.Environment;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
    private ArrayList<Abbreviation> abbreviations = null;
    public Importer() {
        // check for existing tables and create them, if not existent (for mode DEV and TEST)
        if(Environment.getActive().equals(Environment.DEVELOPMENT) || Environment.getActive().equals(Environment.TEST)) {
            DbHelper.createMissingTables();
        }
        abbreviations = new ArrayList<>();
    }

    /**
     *
     * @param innerChild
     * @return string of inner html content
     */
    private String extractInnerHtml(Node innerChild) {
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
        return outStr.toString();
    }

    private void parseAbbrvData(NodeList entries) {
        NodeList abbrvContent = null;
        for(int i = 0; i < entries.item(0).getChildNodes().getLength(); i++) {
            if(entries.item(0).getChildNodes().item(i).getNodeName().equals("text"))
                abbrvContent = entries.item(0).getChildNodes().item(i).getChildNodes();
        }
        ArrayList<Abbreviation> inpAbbrvs = new ArrayList<>();
        if(abbrvContent != null) {
            for(int c = 0; c < abbrvContent.getLength(); c++) {
                Node abbrvNode = abbrvContent.item(c);
                if(abbrvNode.getNodeName().equals("p")) {
                    //extract html code from node
                    String abbrvLine = this.extractInnerHtml(abbrvNode);
                    //select all abbrv short formes within the <b> tags
                    String abbrvRaw = abbrvLine.substring(abbrvLine.indexOf("<b>"), abbrvLine.lastIndexOf("</b>") + 4);
                    //skip the '*' entry
                    if(abbrvRaw.substring(abbrvRaw.indexOf("<b>") + 3, abbrvRaw.indexOf("</b>")).equals("*")) {
                        continue;
                    }
                    String abbrvLabel = null;
                    String abbrv = null;
                    //more than one abbreviation in a line, separated by comma
                    if(abbrvRaw.contains(",")) {
                        String[] abbrvRaws = abbrvRaw.split(",");
                        //select the abbrv labels (everything after the last </b> till the start of the <p> tag minus 1)
                        // -1 for the trailing dot
                        abbrvLabel = abbrvLine.substring(abbrvLine.lastIndexOf("</b>") + 6,abbrvLine.indexOf("</p>") - 1).replace("od.", "oder").trim();
                        String[] abbrvLabels = abbrvLabel.split(",");
                        for(int a = 0; a < abbrvRaws.length; a ++) {
                            abbrv = abbrvRaws[a].substring(abbrvRaws[a].indexOf("<b>") + 3,abbrvRaws[a].indexOf("</b>"));
                            abbrvLabel = abbrvLabels.length == 1 ? abbrvLabels[abbrvLabels.length - 1].trim() : abbrvLabels[a].trim();
                            inpAbbrvs.add(Abbreviation.create(abbrv, abbrvLabel));
                        }
                    } else {
                        abbrv = abbrvRaw.substring(abbrvRaw.indexOf("<b>") + 3, abbrvRaw.indexOf("</b>"));
                        abbrvLabel = abbrvLine.substring(abbrvLine.lastIndexOf("</b>") + 6,abbrvLine.indexOf("</p>") - 1).replace("od.", "oder").trim();
                        inpAbbrvs.add(Abbreviation.create(abbrv, abbrvLabel));
                    }
                }
            }
            logger.info("Anzahl Abbrvs: " + inpAbbrvs.size());
            for(int i = 0; i < inpAbbrvs.size(); i++) {
                AbbrvImportTransaction abbrvImport = new AbbrvImportTransaction(inpAbbrvs.get(i));
                //DbHelper.transaction(abbrvImport);
                //inpAbbrvs.set(i,abbrvImport.getCurrentA());
                logger.info(inpAbbrvs.get(i));
            }
        }
        this.abbreviations = inpAbbrvs;
    }


    private void parseEntryData(NodeList entries) throws IOException, SAXException {
        //foreach entry
        Optional<DictionaryEntry> prevEntry = Optional.absent();
        long currentEntryCounter = 0;
        for(int i = 0; i < entries.getLength(); i++) {
            long id = ++currentEntryCounter;
            if(currentEntryCounter > 3)
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
                            String html = this.extractInnerHtml(innerChild);
                            //look for abbreviations in the description and add <abbrv></abbrv> tags
                            String lastAbbrv = "";
                            for(int a  = 0; a < this.abbreviations.size(); a++) {
                                String abbrvBuf = this.abbreviations.get(a).getShortForm();
                                //don't search for a short name more than once (for instance n. = nach. & n. = generis neutrius.)
                                if(lastAbbrv.equals(abbrvBuf))
                                    continue;
                                if(html.contains(" " + abbrvBuf)) {
                                    logger.info("HAHA: " + abbrvBuf);
                                    html = html.replace(" " + abbrvBuf," <abbrv>" + abbrvBuf + "</abbrv>");
                                }
                                lastAbbrv = abbrvBuf;
                            }
                            //WTF java: adding NULL as a word ?
                            if(descriptionHtml == null)
                                descriptionHtml = html;
                            else
                                descriptionHtml += html;
                        }
                    }
                    //strips all the html tags and unescape resulting string from cleaner
                    description = StringEscapeUtils.unescapeHtml4(Jsoup.clean(descriptionHtml, Whitelist.none()));
                }
            }
            DictionaryEntry currentEntry = DictionaryEntry.create(integerAbsent, integerAbsent, keyGroupIndex, keyword, description, Optional.of(descriptionHtml), wordType);
            //save entry to the database within a transaction and return the new previous
            //entry for referencing in the next iteration
            EntryImportTransaction entryImp = new EntryImportTransaction(prevEntry,currentEntry);
            DbHelper.transaction(entryImp);
            prevEntry = entryImp.getPrevE();
            logger.info(prevEntry.get().getHtmlDescription());
            /* TODO: when implemented, activate these setters:
              currentEntry.setWordType(wordType);*/
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
                        if (fileHandle.getName().contains("000")) {
                            //search for string 'Verzeichnis' in the metadata articles
                            this.parseAbbrvData((NodeList) XPathFactory.newInstance().newXPath().evaluate("//article[contains(lem,'Verzeichnis')]",
                                    content.getDocumentElement(),
                                    XPathConstants.NODESET));
                        }
                        else {
                            // all entries in the file
                            this.parseEntryData(content.getElementsByTagName("article"));
                        }
                    } catch (ParserConfigurationException | XPathExpressionException e) { // shouldn't be thrown with default configuration
                        e.printStackTrace();
                    }
                }
                //break;
            }
        }
    }

    public class AbbrvImportTransaction extends DbHelper.Operations<Object> {
        private Abbreviation currentA;

        public AbbrvImportTransaction(Abbreviation currentAbbrv) {
            currentA = currentAbbrv;
        }

        @Override
        public Object perform(Session tx) {
            List<Abbreviation> duplicates = currentA.crud(tx).duplicateList();
            if (duplicates.isEmpty()) // no duplicate entries!
                currentA = currentA.crud(tx).insertAsNew();
            else {
                String dupList = Joiner.on(" \n").join(duplicates);
                logger.warn(format("Already found abbreviations in database with same content as %s:%n%s%n - SKIPPED",
                        currentA, dupList));
            }
            return null;
        }
        public Abbreviation getCurrentA() {
            return this.currentA;
        }
    }
    public class EntryImportTransaction extends DbHelper.Operations<Object> {
        private Optional<DictionaryEntry> prevE;
        private DictionaryEntry currentE;

        public EntryImportTransaction(Optional<DictionaryEntry> prevEntry, DictionaryEntry currentEntry) {
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

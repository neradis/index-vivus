package de.fusionfactory.index_vivus.xmlimport;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.Environment;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.AbbreviationOccurrence;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
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
import java.io.FileNotFoundException;
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
public abstract class Importer {
    protected static Logger logger = Logger.getLogger(Importer.class);
    protected final Optional<Integer> integerAbsent = Optional.absent();
    protected final Optional<String> stringAbsent = Optional.absent();
    protected ArrayList<Abbreviation> abbreviations = null;

    public Importer() {
        // check for existing tables and create them, if not existent (for mode DEV and TEST)
        if (Environment.getActive().equals(Environment.DEVELOPMENT) || Environment.getActive().equals(Environment.TEST)) {
            DbHelper.createMissingTables();
        }
        abbreviations = new ArrayList<>();
    }

    public Importer(List<Abbreviation> knownAbbreviations) {
        this();
        abbreviations.addAll(knownAbbreviations);
    }

    protected abstract Language sourceLanguage();

    protected abstract String sourceFilePrefix();

    /**
     * @param innerChild
     * @return string of inner html content
     */
    protected String extractInnerHtml(Node innerChild) {
        StringWriter outStr = new StringWriter();
        Transformer transformer;
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

    protected abstract void parseAbbrvData(NodeList entries);

    private void parseEntryData(NodeList entries) throws IOException, SAXException {
        //foreach entry
        Optional<DictionaryEntry> prevEntry = Optional.absent();
        long currentEntryCounter = 0;
        for (int i = 0; i < entries.getLength(); i++) {
            currentEntryCounter++;
            //TODO: comment in for testing, commented out only for the commit!
            //if(currentEntryCounter > 3)
            //    break;
            String keyword = "", description = "", descriptionHtml = null;
            WordType wordType = WordType.UNKNOWN;
            byte keyGroupIndex = 1;

            NodeList entryContent = entries.item(i).getChildNodes();
            ArrayList<Abbreviation> abbrvMatches = new ArrayList<>();
            for (int c = 0; c < entryContent.getLength(); c++) {
                Node childContent = entryContent.item(c);
                // keyword
                if (childContent.getNodeName().equals("lem")) {
                    String buf = childContent.getFirstChild().getNodeValue().trim();
                    //personal name ergo noun
                    if (Character.isUpperCase(buf.charAt(0)))
                        wordType = WordType.NOUN;

                    if (buf.contains("[") && !buf.contains("[*]")) {
                        //keyword without the ordinal number
                        keyword = buf.substring(0, buf.indexOf("[")).trim();
                        //take the number within square brackets
                        keyGroupIndex = Byte.parseByte(buf.substring(buf.indexOf("[") + 1, buf.indexOf("]")));
                    } else if (buf.contains("[*]"))  //@TODO find out what this means for an entry and if information is needed
                        keyword = buf.substring(0, buf.indexOf("[")).trim();
                    else
                        keyword = buf;
                }

                // text content containing one or no header element (<h3>) and a <p> element
                else if (childContent.getNodeName().equals("text")) {
                    NodeList descContentNodes = childContent.getChildNodes();
                    for (int d = 0; d < descContentNodes.getLength(); d++) {
                        Node innerChild = descContentNodes.item(d);
                        //one of the description for the entry
                        if (innerChild.getNodeName().equals("p")) {
                            String html = this.extractInnerHtml(innerChild);
                            //look for abbreviations in the description and add <abbrv></abbrv> tags
                            String lastAbbrv = "";
                            for (Abbreviation abbrv : this.abbreviations) {
                                String aBuf = abbrv.getShortForm();
                                //don't search for a short name more than once (for instance n. = nach. & n. = generis neutrius.)
                                if (lastAbbrv.equals(aBuf))
                                    continue;
                                //abbreviation match, edit htmlDescription and add to list
                                if (html.contains(" " + aBuf)) {
                                    //logger.info("HAHA: " + aBuf);
                                    html = html.replace(" " + aBuf, " <abbrv>" + aBuf + "</abbrv>");
                                    // no duplicate matches are tracked!
                                    if (!abbrvMatches.contains(abbrv))
                                        abbrvMatches.add(abbrv);
                                }
                                lastAbbrv = aBuf;
                            }
                            //WTF java: adding NULL as a word ?
                            if (descriptionHtml == null)
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
            EntryImportTransaction entryImp = new EntryImportTransaction(prevEntry, currentEntry);
            DbHelper.transaction(entryImp);

            //prevEntry is the processed currentEntry !
            prevEntry = entryImp.getPrevE();
            // save all the abbreviation occurrences in the current entry
            logger.info(prevEntry.get());
            logger.info(abbrvMatches.size());
            for (Abbreviation abbrv : abbrvMatches) {
                AbbreviationOccurrenceImportTransaction abbrvOccImp = new AbbreviationOccurrenceImportTransaction(prevEntry.get(), abbrv);
                DbHelper.transaction(abbrvOccImp);
            }
            /* TODO: when implemented, activate these setters:
              currentEntry.setWordType(wordType);*/
        }

    }

    public void importFromDefaultLocation() throws IOException, SAXException {
        importDir(LocationProvider.getInstance().getDictionaryDir());
    }

    public void importDir(String dirPath) throws IOException, SAXException {
        importDir(new File(dirPath));
    }

    public void importDir(File inputDir) throws IOException, SAXException {
        File[] files = inputDir.listFiles();
        boolean noFiles = true;

        if (files != null) {
            for (File fileHandle : files) {
                if (fileHandle.isFile() && fileHandle.getName().startsWith(sourceFilePrefix())
                        &&fileHandle.getName().contains(".xml")) {
                    logger.info("Datei: '" + fileHandle.getAbsoluteFile() + "' wird verarbeitet.");
                    noFiles = false;
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
                        } else {
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
        if(noFiles) {
            throw new FileNotFoundException("Keine passenden Wörterbuchdateien gefunden");
        }
    }

    public List<Abbreviation> getAbbreviations() {
        return this.abbreviations;
    }

    public class AbbreviationOccurrenceImportTransaction extends DbHelper.Operations<Object> {
        private DictionaryEntry entry;
        private Abbreviation abbrv;

        public AbbreviationOccurrenceImportTransaction(DictionaryEntry relevantEntry, Abbreviation relevantAbbreviation) {
            entry = relevantEntry;
            abbrv = relevantAbbreviation;
        }

        @Override
        public Object perform(Session tx) {
            //check if already existent
            if (!AbbreviationOccurrence.exists(entry.getId(), abbrv.getId(), tx)) // no duplicate entry
                AbbreviationOccurrence.create(entry.getId(), abbrv.getId(), tx);
            else {
                logger.warn(format("Skipping already added abbreviation occurrence relation: %s#%d - %s",
                        entry.getKeyword(), entry.getKeywordGroupIndex(), abbrv.getShortForm()));
            }
            return null;
        }
    }

    public class AbbreviationImportTransaction extends DbHelper.Operations<Object> {
        private Abbreviation currentA;

        public AbbreviationImportTransaction(Abbreviation currentAbbrv) {
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
                    currentE.setPreviousEntryId(Optional.of(prevE.get().getId()));
                }
                currentE = currentE.crud(tx).insertAsNew();
                if (prevE.isPresent()) {
                    prevE.get().setNextEntryId(currentE.getIdOptional());
                    prevE.get().crud(tx).update();
                }
                //set current Entry as previous Entry
                logger.info("prev: " + prevE);
                logger.info("current: " + currentE);
                prevE = Optional.of(currentE);
            } else {
                String dupList = Joiner.on(" \n").join(duplicates);
                logger.warn(format("Already found entries in database with same content as %s:%n%s%n - SKIPPED",
                        currentE, dupList));
            }
            return null;
        }

        public Optional<DictionaryEntry> getPrevE() {
            return prevE;
        }
    }
}

package de.fusionfactory.index_vivus.xmlimport;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.fusionfactory.index_vivus.configuration.Environment;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.WordType;
import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.tools.scala.Utils;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static de.fusionfactory.index_vivus.services.Language.GREEK;
import static de.fusionfactory.index_vivus.services.Language.LATIN;
import static java.lang.String.format;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 08.12.13
 * Time: 22:55
 */
public abstract class Importer {
    protected static Logger logger = Logger.getLogger(Importer.class);
    protected final Optional<Integer> integerAbsent = Optional.absent();
    protected final short logEntryBatchSize = 1000;
    protected final String abbrTag = "abbr";
    private AtomicReference<ImmutableSet<String>> abbrShortForms = new AtomicReference<>();


    public static ImmutableMap<Language, ImmutableSet<String>> DOT_STRIP_EXCEPTIONS = ImmutableMap.of(
            LATIN, ImmutableSet.of("A.", "a. a. O.", "a. E.", "Adverb.", "altert.", "dass.", "L.",
                    "latin.", "leb.", "n.", "s. v. a.", "Verb."),
            GREEK, ImmutableSet.of("B.A.", "D.L.", "E.G.", "E.M.", "H.", "K.S.")
    );


    public Importer() {
        // check for existing tables and create them, if not existent (for mode DEV and TEST)

        if (Environment.getActive().equals(Environment.DEVELOPMENT) || Environment.getActive().equals(Environment.TEST)) {
            DbHelper.createMissingTables();
        }
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

    protected ImmutableSet<String> getAbbrShortForms() {
        synchronized (abbrShortForms) {
            if (abbrShortForms.get() == null) {
                abbrShortForms.compareAndSet(null, collectShortForms());
            }
        }
        return abbrShortForms.get();
    }

    public static boolean testIfShortFormDotStrippable(String shortForm, Language lang) {
        return shortForm.endsWith(".") && shortForm.length() > 2 &&
                !DOT_STRIP_EXCEPTIONS.get(lang).contains(shortForm);
    }

    private ImmutableSet<String> collectShortForms() {
        List<Abbreviation> abbrevs = Abbreviation.fetchAll();
        Set<String> res = Sets.newHashSetWithExpectedSize(abbrevs.size() * 2);
        for (Abbreviation abbr : abbrevs) {
            String sf = abbr.getShortForm();
            res.add(sf);
            if (testIfShortFormDotStrippable(sf, sourceLanguage())) {
                res.add(sf.substring(0, sf.length() - 1));
            }
        }
        return ImmutableSet.copyOf(res);
    }

    protected abstract void parseAbbrvData(NodeList entries);

    private void parseEntryData(NodeList entries) throws IOException, SAXException {
        //foreach entry
        Optional<DictionaryEntry> prevEntry = Optional.absent();
        long processed = 0, added = 0, logCounter = 0, count = entries.getLength();
        short warningsPrinted = 0;
        logger.info("Input entries in file: " + count);
         /* build up abbreviation prefix tree (add all abbreviations) for one time
             - whole words to skip cases with the abbreviation as part/in the end of a longer one.
             - remove overlaps to find longer abbreviations before shorter ones
               with the same suffix (e.g. 'Alex. Aet.' before 'Alex.' */
        Trie abbrTrie = new Trie().onlyWholeWords().removeOverlaps();
        for (String sf : getAbbrShortForms())
            abbrTrie.addKeyword(sf);

        LinkedHashMap<DictionaryEntry, ArrayList<Abbreviation>> entryBulk = new LinkedHashMap<>();
        for (int i = 0; i < entries.getLength(); i++) {
            // DEBUG stop after x entries
            // if(processed > 500)
            //     break;
            String keyword = "", description = "", descriptionHtml = null;
            WordType wordType = WordType.UNKNOWN;
            //is only higher than one with more than one usage for an abbreviation short form
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

                    } else if (buf.contains("[*]"))
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
                            StringBuffer html = new StringBuffer();
                            /* with tokenize we can work on the fly with the matches, so we don't need parseText()
                            Collection<Emit> abbrvFounds = abbrTrie.parseText(this.extractInnerHtml(innerChild));
                            for(Emit e : abbrvFounds)
                                logger.info(e.getKeyword() + " " + e.getStart() + " " + e.getEnd());*/

                            Collection<Token> htmlTokens = abbrTrie.tokenize(this.extractInnerHtml(innerChild));
                            //add each token to the result buffer, for an abbreviation match with <abbr> Tag
                            for (Token htmlT : htmlTokens) {
                                //logger.info(htmlT.getFragment() + " " + htmlT.isMatch());
                                if (htmlT.isMatch())
                                    html.append("<" + this.abbrTag + ">");
                                html.append(htmlT.getFragment());
                                if (htmlT.isMatch())
                                    html.append("</" + this.abbrTag + ">");
                            }
                            //WTF java: adding NULL as a word?
                            if (descriptionHtml == null)
                                descriptionHtml = html.toString();
                            else
                                descriptionHtml += html.toString() + "\n";
                        }
                    }
                    descriptionHtml = descriptionHtml.trim();
                    //strips all the html tags and unescape resulting string from cleaner
                    description = StringEscapeUtils.unescapeHtml4(Jsoup.clean(descriptionHtml, "",
                            Whitelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false)));
                }
            }
            //add entry to the bulk linkedHashMap with the dictionary entry and the related list of abbreviation matches
            entryBulk.put(DictionaryEntry.create(sourceLanguage(), integerAbsent, integerAbsent,
                    keyGroupIndex, keyword, description,
                    Optional.of(descriptionHtml), wordType), abbrvMatches);
            processed++;
            logCounter++;
            if (logCounter >= logEntryBatchSize) {
                logCounter = 0;
                //save bulk of entries to the database within a transaction and return the new previous
                //entry for referencing in the next iteration
                EntryImportTransaction entryImp = new EntryImportTransaction(prevEntry, entryBulk, warningsPrinted);
                //prevEntry is the last element of the current entry bulk

                DbHelper.transaction(entryImp);
                prevEntry = entryImp.getPrevE();
                // warningsPrinted = entryImp.getWarningE();
                added += entryImp.getAdded();
                logger.info(processed + " of " + count + " entries processed...");
                //clear bulk list for the next amount of dictionary entries
                entryBulk = new LinkedHashMap<>();
                System.gc();
            }
        }
        if (entryBulk.size() != 0) {
            EntryImportTransaction entryImp = new EntryImportTransaction(prevEntry, entryBulk, warningsPrinted);
            DbHelper.transaction(entryImp);
            added += entryImp.getAdded();
        }
        logger.info("Entries processed: " + processed);
        logger.info("Dictionary entries added: " + added);
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
            ImmutableList<File> importFiles = Utils.moveMetaDataFilesToFront(files);
            logger.info("Will import the following files in order as listed:\n" + Joiner.on("\n").join(importFiles));

            for (File fileHandle : importFiles) {
                if (fileHandle.isFile() && fileHandle.getName().startsWith(sourceFilePrefix())
                        && fileHandle.getName().contains(".xml")) {

                    logger.info("Processing '" + fileHandle.getAbsoluteFile() + "'...");
                    noFiles = false;
                    try {
                        DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
                        docBuildFac.setIgnoringComments(true);
                        DocumentBuilder docBuild = docBuildFac.newDocumentBuilder();
                        Document content = docBuild.parse(fileHandle);
                        content.normalizeDocument();
                        //first file in a collection starts with 000 and contains meta info
                        if (fileHandle.getName().contains("000")) {
                            logger.info("Metadata file...");
                            //search for string 'Verzeichnis' in the metadata articles
                            this.parseAbbrvData((NodeList) XPathFactory.newInstance().newXPath().evaluate("//article[contains(lem,'Verzeichnis')]",
                                    content.getDocumentElement(),
                                    XPathConstants.NODESET));
                        } else {
                            logger.info("Regular data file...");
                            // all entries in the file
                            this.parseEntryData(content.getElementsByTagName("article"));
                        }
                    } catch (ParserConfigurationException | XPathExpressionException e) { // shouldn't be thrown with default configuration
                        e.printStackTrace();
                    }
                }
            }
        }
        if (noFiles) {
            throw new FileNotFoundException("Keine passenden Wörterbuchdateien gefunden");
        }
    }

    public class AbbreviationsImportTransaction extends DbHelper.Operations<Object> {
        private final byte warningThreshold = 10;
        private ArrayList<Abbreviation> abbreviations;

        public AbbreviationsImportTransaction(ArrayList<Abbreviation> currentAbbrvs) {
            abbreviations = currentAbbrvs;
        }

        @Override
        public Object perform(Session tx) {
            long added = 0;
            short warningC = 0;
            for (int a = 0; a < abbreviations.size(); a++) {
                Abbreviation abbrv = abbreviations.get(a);
                List<Abbreviation> duplicates = abbrv.crud(tx).duplicateList();
                Abbreviation abbrvWithId;
                if (duplicates.isEmpty()) {
                    abbrvWithId = abbrv.crud(tx).insertAsNew();
                    added++;
                } else {
                    if (warningC == warningThreshold) {
                        logger.warn("Too many abbreviation warnings occurred, rerun on similar data is assumed." +
                                "\nFurther abbreviation related warnings are suppressed!");
                        warningC++;
                    } else if (warningC < warningThreshold) {
                        String dupList = Joiner.on(" \n").join(duplicates);
                        logger.warn(format("Already found abbreviations in database with same content as %s:%n%s%n - SKIPPED",
                                abbrv, dupList));
                        warningC++;
                    }
                    //take entry from database for id instead of insert a new one
                    //because of the warning it's safe to use get() without check
                    abbrvWithId = Abbreviation.fetchByShortForm(abbrv.getShortForm()).get();
                }
                abbreviations.set(a, abbrvWithId);

            }
            logger.info("Abbreviations added: " + added);
            return null;
        }

        public ArrayList<Abbreviation> getAbbreviations() {
            return this.abbreviations;
        }
    }

    public class EntryImportTransaction extends DbHelper.Operations<Object> {
        private Optional<DictionaryEntry> prevE;
        private LinkedHashMap<DictionaryEntry, ArrayList<Abbreviation>> currentEntryBulk;
        private int added;
        static final short warningThreshold = 10;
        private short warningE;

        public EntryImportTransaction(Optional<DictionaryEntry> prevE, LinkedHashMap<DictionaryEntry, ArrayList<Abbreviation>> currentEntryBulk,
                                      short warningE) {
            this.prevE = prevE;
            this.currentEntryBulk = currentEntryBulk;
            this.added = 0;
            this.warningE = warningE;
        }

        @Override
        public Object perform(Session tx) {
            for (Map.Entry<DictionaryEntry, ArrayList<Abbreviation>> bulkEntry : currentEntryBulk.entrySet()) {
                DictionaryEntry currentE = bulkEntry.getKey();
                ArrayList<Abbreviation> abbrvMatches = bulkEntry.getValue();
                //empty list to avoid duplicate checks for entries (very costly ;))
                //TODO: reworking of this behavior
                List<DictionaryEntry> duplicates = ImmutableList.of();//currentE.crud(tx).duplicateList();
                if (duplicates.isEmpty()) { // no duplicate entries!
                    if (prevE.isPresent()) {

                        currentE.setPreviousEntryId(Optional.of(prevE.get().getId()));
                    }
                    currentE = currentE.crud(tx).insertAsNew();
                    if (prevE.isPresent()) {
                        prevE.get().setNextEntryId(currentE.getIdOptional());
                        prevE.get().crud(tx).update();
                    }
                    added++;
                } else {
                    if (warningE == warningThreshold) {
                        logger.warn("Too many dictionary entry warnings occurred, rerun on similar data is assumed." +
                                "\nFurther dictionary entry  related warnings are suppressed!");
                        warningE++;
                    } else if (warningE < warningThreshold) {
                        String dupList = Joiner.on(" \n").join(duplicates);
                        logger.warn(format("Already found entries in database with same content as dictionary entry %s(%d):%n%s%n - SKIPPED",
                                currentE.getKeyword(), currentE.getKeywordGroupIndex(), dupList));
                        warningE++;
                    }
                    //take entry from database for id instead of inserting it as a new one
                    //because of the warning it's safe to use get() without check
                    currentE = DictionaryEntry.fetchByKeywordAndGroupId(currentE.getKeyword(),
                            currentE.getKeywordGroupIndex()).get();
                }

                //set current Entry as previous Entry
                prevE = Optional.of(currentE);
            }
            return null;
        }

        public Optional<DictionaryEntry> getPrevE() {
            return prevE;
        }

        public int getAdded() {
            return added;
        }

        public short getWarningE() {
            return warningE;
        }
    }

    public static void main(String[] args) {
        List<Importer> importers = ImmutableList.of(new GeorgesImporter(), new PapeImporter());
        for (Importer importer : importers) {
            try {
                importer.importFromDefaultLocation();
            } catch (IOException | SAXException ex) {
                logger.warn(format("Import for dictionary failed due to:%n%s", ex.getMessage()));
            }
        }
    }
}

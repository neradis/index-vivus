package de.fusionfactory.index_vivus.xmlimport;

import de.fusionfactory.index_vivus.models.scalaimpl.Abbreviation;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Kevin Jakob <kevin-jakob@web.de>
 * Date: 22.01.14
 * Time: 23:38
 */
public class PapeImporter extends Importer{
    private final String hivenCP = "\u2013";
    public PapeImporter() {
        super();
    }

    @Override
    protected String sourceFilePrefix() {
        return "Pape";
    }

    @Override
    protected Language sourceLanguage() {
        return Language.GREEK;
    }

    @Override
    protected void parseAbbrvData(NodeList entries) {
        NodeList abbrvContent = null;
        long processed = 0;
        for (int i = 0; i < entries.item(0).getChildNodes().getLength(); i++) {
            if (entries.item(0).getChildNodes().item(i).getNodeName().equals("text"))
                abbrvContent = entries.item(0).getChildNodes().item(i).getChildNodes();
        }
        if (abbrvContent != null) {
            // tree map with custom comparator being not case sensitive
            TreeMap<String, String> inpAbbrvs = new TreeMap<>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.toLowerCase().compareTo(o2.toLowerCase());
                }
            });
            int errorC = 0;
            boolean first = true;
            String continuingAbbr = null;
            for (int c = 0; c < abbrvContent.getLength(); c++) {
                Node abbrvNode = abbrvContent.item(c);
                if (abbrvNode.getNodeName().equals("p")) {
                    //first p is useless some generic information
                    if(first) {
                        first = false;
                        continue;
                    }
                    //extract html code from node
                    String abbrvLine = this.extractInnerHtml(abbrvNode);
                    String abbrvLabel, abbrv;

                    //we have a continuing line and no false positive (needs to have no hiven)
                    if(continuingAbbr != null && !abbrvLine.contains(hivenCP)) {
                        String[] abbrvRaws = continuingAbbr.split(",");
                        abbrvLabel = abbrvLine.substring(abbrvLine.indexOf(">") + 1, abbrvLine.indexOf("</p>")).replace("od.", "oder").trim();
                        //set continuingAbbr to null if abbrLabel ends in current line
                        if(abbrvLabel.endsWith("."))
                            continuingAbbr = null;
                        abbrvLabel = abbrvLabel.substring(0, abbrvLabel.length() - 1);
                        for (int a = 0; a < abbrvRaws.length; a ++) {
                            abbrv = abbrvRaws[a].trim();
                            //some semantic checks ( '-' or '=' at the end of the line)
                            if(inpAbbrvs.get(abbrv).endsWith("-"))
                                inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv).substring(0,inpAbbrvs.get(abbrv).length() - 1) + abbrvLabel);
                            else if(inpAbbrvs.get(abbrv).endsWith("="))
                                inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv) + " " + abbrvLabel);
                            else
                                inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv) + abbrvLabel);
                        }
                        processed ++;
                        continue;
                    }
                    // \u2013 is the unicode '-', if not existent in the line then ignore
                    else if(!abbrvLine.contains(hivenCP)) {
                        processed++;
                        errorC++;
                        continue;
                    }

                    //select all abbrv short formes (all up to the hiven)
                    //trim some insignificant chars and normalize delimiters
                    String abbrvRaw = abbrvLine.substring(abbrvLine.indexOf(">") + 1,abbrvLine.indexOf(hivenCP))
                                            .trim()
                                            .replaceAll("(\\(|\\))","")
                                            .replaceAll("( od\\.|oder)",",");

                    //select the abbrv label (everything after the hiven till the start of the <p> tag)
                    abbrvLabel = abbrvLine.substring(abbrvLine.indexOf(hivenCP) + 1, abbrvLine.indexOf("</p>")).replace("od.", "oder").trim();
                    //if no trailing dot then abbrLabel is continued in the next line
                    if(!abbrvLabel.endsWith(".")  && !abbrvLabel.endsWith(")"))
                        //using abbrvRaw gives us to add the next abbrvLabel line to all abbrvs in the current line (if there are commas)
                        continuingAbbr = abbrvRaw;
                    else
                        abbrvLabel = abbrvLabel.substring(0, abbrvLabel.length() - 1);

                    String[] abbrvRaws = abbrvRaw.split(",");
                    for (int a = 0; a < abbrvRaws.length; a++) {
                        abbrv = abbrvRaws[a].trim();
                        if (inpAbbrvs.containsKey(abbrv))
                            inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv) + " oder " + abbrvLabel);
                        else
                            inpAbbrvs.put(abbrv, abbrvLabel);
                    }
                    processed ++;
                }
            }
            ArrayList<Abbreviation> aBuf = new ArrayList<>();
            for (Map.Entry<String, String> entry : inpAbbrvs.entrySet())
                aBuf.add(Abbreviation.create(sourceLanguage(), entry.getKey(), entry.getValue()));
            logger.info("Lines processed: " + processed);
            logger.info("Thereof invalid: " + errorC);
            AbbreviationsImportTransaction abbrvImport = new AbbreviationsImportTransaction(aBuf);
            DbHelper.transaction(abbrvImport);

            this.abbreviations.addAll(abbrvImport.getAbbreviations());
            //debug output
            //for (Abbreviation abbrv : this.abbreviations)
            //    logger.debug(abbrv);
        }
    }
    public static void main (String args[]) {
        Importer test = new PapeImporter();
        try {
            test.importFromDefaultLocation();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}

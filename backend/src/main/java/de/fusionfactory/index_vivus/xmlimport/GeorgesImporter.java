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
 * Time: 23:35
 */
public class GeorgesImporter extends Importer {

    @Override
    protected String sourceFilePrefix() {
        return "Georges";
    }

    @Override
    protected Language sourceLanguage() {
        return Language.LATIN;
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

            for (int c = 0; c < abbrvContent.getLength(); c++) {
                Node abbrvNode = abbrvContent.item(c);
                if (abbrvNode.getNodeName().equals("p")) {
                    //extract html code from node
                    String abbrvLine = this.extractInnerHtml(abbrvNode);
                    //select all abbrv short formes within the <b> tags
                    String abbrvRaw = abbrvLine.substring(abbrvLine.indexOf("<b>"), abbrvLine.lastIndexOf("</b>") + 4);
                    //skip the '*' entry
                    if (abbrvRaw.substring(abbrvRaw.indexOf("<b>") + 3, abbrvRaw.indexOf("</b>")).equals("*")) {
                        processed++;
                        continue;
                    }
                    String abbrvLabel, abbrv;
                    //more than one abbreviation in a line, separated by comma
                    if (abbrvRaw.contains(",")) {
                        String[] abbrvRaws = abbrvRaw.split(",");
                        //select the abbrv labels (everything after the last </b> till the start of the <p> tag minus 1)
                        // -1 for the trailing dot
                        abbrvLabel = abbrvLine.substring(abbrvLine.lastIndexOf("</b>") + 6, abbrvLine.indexOf("</p>") - 1).replace("od.", "oder").trim();
                        String[] abbrvLabels = abbrvLabel.split(",");
                        for (int a = 0; a < abbrvRaws.length; a++) {
                            abbrv = abbrvRaws[a].substring(abbrvRaws[a].indexOf("<b>") + 3, abbrvRaws[a].indexOf("</b>"));
                            abbrvLabel = abbrvLabels.length == 1 ? abbrvLabels[abbrvLabels.length - 1].trim() : abbrvLabels[a].trim();
                            if (inpAbbrvs.containsKey(abbrv))
                                inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv) + " oder " + abbrvLabel);
                            else
                                inpAbbrvs.put(abbrv, abbrvLabel);
                        }
                    } else {
                        abbrv = abbrvRaw.substring(abbrvRaw.indexOf("<b>") + 3, abbrvRaw.indexOf("</b>"));
                        abbrvLabel = abbrvLine.substring(abbrvLine.lastIndexOf("</b>") + 6, abbrvLine.indexOf("</p>") - 1).replace("od.", "oder").trim();
                        if (inpAbbrvs.containsKey(abbrv))
                            inpAbbrvs.put(abbrv, inpAbbrvs.get(abbrv) + " oder " + abbrvLabel);
                        else
                            inpAbbrvs.put(abbrv, abbrvLabel);
                    }
                    processed++;
                }
            }
            ArrayList<Abbreviation> aBuf = new ArrayList<>();
            for (Map.Entry<String, String> entry : inpAbbrvs.entrySet())
                aBuf.add(Abbreviation.create(sourceLanguage(), entry.getKey(), entry.getValue()));
            logger.info("Processed abbreviations: " + processed);
            AbbreviationsImportTransaction abbrvImport = new AbbreviationsImportTransaction(aBuf);
            DbHelper.transaction(abbrvImport);

            //debug output
            this.abbreviations.addAll(abbrvImport.getAbbreviations());
            for (Abbreviation abbrv : this.abbreviations)
                logger.debug(abbrv);
        }
    }

    public static void main(String[] args) {
        try {
            new GeorgesImporter().importFromDefaultLocation();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}

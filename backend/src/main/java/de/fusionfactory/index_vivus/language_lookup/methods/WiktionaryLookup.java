package de.fusionfactory.index_vivus.language_lookup.methods;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import de.fusionfactory.index_vivus.language_lookup.WordNotFoundException;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 16.01.14
 * Time: 16:17
 */
public class WiktionaryLookup extends LookupMethod {
    private Logger logger = Logger.getLogger(WiktionaryLookup.class);

    public WiktionaryLookup(Language expectedLanguage) {
        super(expectedLanguage);
        if (!mapWiktionaryLanguageKeys().containsKey(expectedLanguage)) {
            throw new RuntimeException("This Lookup method only supports german language.");
        }
    }

    /**
     * Maps how wiktionary represent the language tagging.
     *
     * @return
     */
    private static Map<Language, String> mapWiktionaryLanguageKeys() {
        final Map<Language, String> map = new HashMap<>();
        map.put(Language.GERMAN, "German");

        return map;
    }

    /**
     * Check if the word is child of given language.
     *
     * @param word
     * @return
     */
    @Override
    public boolean isExpectedLanguage(String word) {
        WiktionaryResponseJson item = doWebRequest(buildApiUri(word));
        if (item == null) {
            return false;
        }

        Iterator it = item.query.pages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            WiktionaryResponseJson.Page p = (WiktionaryResponseJson.Page) e.getValue();
            if (p.categories != null) {
                for (WiktionaryResponseJson.Category c : p.categories) {
                    boolean found = c.title.contains(mapWiktionaryLanguageKeys().get(_language));
                    if (found)
                        return found;
                }
            }
        }
        return false;
    }

    /**
     * not implemented yet.
     *
     * @param word
     * @return
     * @throws WordNotFoundException
     */
    @Override
    public Language getLanguage(String word) throws WordNotFoundException {
        return null;
    }

    /**
     * do the web request through the wiktionary api
     *
     * @param uri
     * @return
     */
    private WiktionaryResponseJson doWebRequest(String uri) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            Thread.currentThread().sleep((long) (Math.random() * (100 - 50)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {
            try {
                HttpGet httpGet = new HttpGet(uri);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = httpClient.execute(httpGet, responseHandler);
                Gson gson = new Gson();
                WiktionaryResponseJson item = gson.fromJson(responseBody, WiktionaryResponseJson.class);

                return item;
            } catch (IOException e) {
                logger.info("Got Exception, retry after 100ms");
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
//				e.printStackTrace();
            } catch (Exception e) {

                logger.info("Got Exception (e), retry after 100ms");
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    /**
     * builds the API Request url
     *
     * @param keyword
     * @return
     */
    private String buildApiUri(String keyword) {
        String uri = null;
        try {
            uri = "http://en.wiktionary.org/w/api.php?action=query&titles=" + URLEncoder.encode(keyword, Charsets.UTF_8.toString()) + "&prop=categories&format=json";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); //will not occurr - UTF-8 is supported everywhere
        }
        return uri;
    }
}

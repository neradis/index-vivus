package de.fusionfactory.index_vivus.indexer;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.language_lookup.Lookup;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCount;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCountImpl$;
import de.fusionfactory.index_vivus.services.scalaimpl.IndexSearch;
import de.fusionfactory.index_vivus.testing.fixtures.LoadFixtures;
import de.fusionfactory.index_vivus.tokenizer.Tokenizer;
import de.fusionfactory.index_vivus.tools.scala.Utils$;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:23
 */
public class Indexer implements IndexSearch {
    private Tokenizer tokenizer = new Tokenizer();
    private File fsDirectoryFile = new File(LocationProvider.getInstance().getDataDir().getPath(), "index.lucene.bin");
    private Directory directoryIndex;
    private static Logger logger = Logger.getLogger(Indexer.class);
    private static Logger preprocLogger = Logger.getLogger("DESCRIPTION_PREPROCESSING");
    private Lookup langLookup = new Lookup(Language.GERMAN);
    public static int TOP_HIT_COUNT = 10;

    public Indexer() {
        logger.info(format("Using %s as directory for Lucene index files", fsDirectoryFile.getAbsolutePath()));

        try {
            directoryIndex = new SimpleFSDirectory(fsDirectoryFile);
        } catch (IOException ioe) {
            throw new FulltextIndexingException("unable to open index directory", ioe);
        }
    }

    public void ensureIndexCreated() throws IOException {
        if (fsDirectoryFile.exists()) {
            logger.info("FSDirectory exists, use it O_o.");
//			fsDirectoryFile.delete();
        } else {
            createIndex();
        }
    }

    private void createIndex() throws IOException {
        logger.info("Create new Index");
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, standardAnalyzer);
        IndexWriter indexWriter = new IndexWriter(directoryIndex, config);

        List<DictionaryEntry> dictionaryEntryList = DictionaryEntry.fetchAll();
        int i = 0;
        for (DictionaryEntry e : dictionaryEntryList) {
            logger.info("progress... " + i);
            insertDocument(indexWriter, e);
            logger.info("progress... " + i + " .. done");
            i++;
        }
        langLookup.shutdown();
        indexWriter.close();
    }

    private void insertDocument(IndexWriter w, DictionaryEntry entry) throws IOException {
        int dbId = entry.getId(), lang = entry.sourceLanguage();

        List<String> tokens = tokenizer.getTokenizedString(entry.getDescription());

        List<String> germanTokens;
        try {
            germanTokens = langLookup.getListOfLanguageWords(tokens);
        } catch (InterruptedException ie) {
            throw new RuntimeException("interrupt in language lookup", ie);
        }

        String content = Joiner.on(' ').join(germanTokens);

        if (preprocLogger.isTraceEnabled()) {
            preprocLogger.trace(format("### processing entry #%d for %s ###", entry.getId(), entry.getKeyword()));
            preprocLogger.trace(format("### original description text:%n%s", entry.description()));
            preprocLogger.trace(format("### tokens after string filtering/expansion:%n%s", Joiner.on(' ').join(tokens)));
            preprocLogger.trace(format("### 'content' for after lang filtering:%n%s", content));
        }

        Document document = new Document();
        document.add(new IntField("DbId", dbId, Field.Store.YES));
        document.add(new IntField("Lang", lang, Field.Store.YES));
        document.add(new TextField("Content", content, Field.Store.NO));

        w.addDocument(document);
    }

    public List<DictionaryEntry> getTopSearchResults(String query) throws IOException, ParseException {
        return getTopSearchResults(query, Language.ALL);
    }

    /**
     * @param query
     * @param language
     * @return
     * @throws ParseException
     * @throws IOException
     */

    public List<DictionaryEntry> getTopSearchResults(String query, Language language) {

        return searchResults(query, language, new SelectorAndResultTransformer<List<DictionaryEntry>>() {
            @Override
            protected ScoreDoc[] selectHits(TopScoreDocCollector collector) {
                return collector.topDocs().scoreDocs;
            }

            @Override
            protected int numberOfDocsToCollect() {
                return TOP_HIT_COUNT;
            }

            @Override
            protected List<DictionaryEntry> transformResults(List<DictionaryEntry> entryHits, int total) {
                return entryHits;
            }
        });
    }

    public DictionaryEntryListWithTotalCount getSearchResults(String query, Language language, final int hitsPerPage,
                                                              final int offset) {

        logger.debug(String.format("paginated fulltext query: query=%s,lang=%s,hitspp=%d,offset=%d",
                query, language, hitsPerPage, offset));

        return searchResults(query, language, new SelectorAndResultTransformer<DictionaryEntryListWithTotalCount>() {
            @Override
            protected ScoreDoc[] selectHits(TopScoreDocCollector collector) {
                return collector.topDocs(offset, hitsPerPage).scoreDocs;
            }

            @Override
            protected int numberOfDocsToCollect() {
                return 10000;
            }

            @Override
            protected DictionaryEntryListWithTotalCount transformResults(List<DictionaryEntry> hitsPage, int total) {
                return DictionaryEntryListWithTotalCountImpl$.MODULE$.apply(hitsPage, total);
            }
        });
    }

    private <R> R searchResults(String query, Language language, SelectorAndResultTransformer<R> selTrans) {

        List<DictionaryEntry> result = Lists.newArrayList();
        if (query.length() < 1) {
            return selTrans.transformResults(result, 0);
        }
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        /*TODO: query terms also have to be processes with the analyser before they can be compared with the keys of
         of the inverted list - verify if TermQuery does so automatically or if we have to do this ourselves*/
        Query q = new BooleanQuery();

        //TODO: use query parse instead of a single TermQuery to enable boolean operators (AND, OR, NOT, etc.)
        QueryParser queryParser = new QueryParser(Version.LUCENE_46, "Content", new StandardAnalyzer(Version.LUCENE_46));
        try {
            Query parsedQuery = queryParser.parse("" + query + "");
            ((BooleanQuery) q).add(parsedQuery, BooleanClause.Occur.MUST);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!language.equals(Language.ALL)) {
            int languageId = Utils$.MODULE$.lang2Byte(language);
            //TODO: check if it's really correct and sound to define the @code{precisionStep} here
            Query query2 = NumericRangeQuery.newIntRange("Lang", 1, languageId, languageId, true, true);
            ((BooleanQuery) q).add(query2, BooleanClause.Occur.MUST);
        }

        logger.trace("query created");

        IndexReader reader;
        try {
            reader = DirectoryReader.open(directoryIndex);
        } catch (IOException ioe) {
            throw new FulltextIndexingException("unable to open index directory", ioe);
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        logger.trace("searcher created");
        TopScoreDocCollector collector = TopScoreDocCollector.create(selTrans.numberOfDocsToCollect(), true);
        logger.trace("collector created");

        TotalHitCountCollector counter = new TotalHitCountCollector();

        try {
            searcher.search(q, collector);
            searcher.search(q, counter);
        } catch (IOException ioe) {
            throw new FulltextIndexingException("error reading from index for search operation", ioe);
        }

        ScoreDoc[] hits = selTrans.selectHits(collector);
        for (ScoreDoc hit : hits) {
            Document d;
            try {
                d = searcher.doc(hit.doc);
            } catch (IOException ioe) {
                throw new FulltextIndexingException("error reading from index to retrieve document infos", ioe);
            }
            int dbId = (int) d.getField("DbId").numericValue();
            Optional<DictionaryEntry> entry = DictionaryEntry.fetchById(dbId);
            if (entry.isPresent()) {
                result.add(entry.get());
            }
        }
        logger.debug(format("returning %d results for query '%s' (language: %s)", result.size(), query, language));

        return selTrans.transformResults(result, collector.getTotalHits());
    }


    private static abstract class SelectorAndResultTransformer<R> {

        protected abstract ScoreDoc[] selectHits(TopScoreDocCollector collector);

        protected abstract R transformResults(List<DictionaryEntry> entryHits, int totalHitCount);

        protected abstract int numberOfDocsToCollect();
    }

    public static class FulltextIndexingException extends RuntimeException {

        public FulltextIndexingException(String message) {
            super(message);
        }

        public FulltextIndexingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void main(String[] args) {
        //need to re-populate memory db for DEVELOPMENT with the fixutres (otherwise there is nothing to index
        LoadFixtures.ensureFixturesForDevelopment();

        try {
            new Indexer().createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

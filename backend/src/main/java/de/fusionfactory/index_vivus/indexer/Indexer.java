package de.fusionfactory.index_vivus.indexer;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.language_lookup.Lookup;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.tokenizer.Tokenizer;
import de.fusionfactory.index_vivus.tools.scala.Utils$;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:23
 */
public class Indexer {
    private Tokenizer tokenizer = new Tokenizer();
    private File fsDirectoryFile = new File(LocationProvider.getInstance().getDataDir().getPath(), "index.lucene.bin");
	private Directory directoryIndex;
    private static Logger logger = Logger.getLogger(Indexer.class);
    private static Logger preprocLogger = Logger.getLogger("DESCRIPTION_PREPROCESSING");
    private Lookup langLookup = new Lookup(Language.GERMAN);
    static int hitsPerPage = 10;

    public Indexer() {
        logger.info(format("Using %s as directory for Lucene index files", fsDirectoryFile.getAbsolutePath()));

        try {
			directoryIndex = new SimpleFSDirectory(fsDirectoryFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void ensureIndexCreated() throws IOException {
        if (fsDirectoryFile.exists()) {
			logger.info("FSDirectory exists, use it O_o.");
		} else {
			createIndex();
		}
	}

	private void createIndex() throws IOException {
        logger.fatal("Create new Index");
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

	public List<DictionaryEntry> getSearchResults(String query) throws IOException, ParseException {
		return getSearchResults(query, Language.ALL);
	}

	public List<DictionaryEntry> getSearchResults(String query, Language language) throws ParseException, IOException {
		List<DictionaryEntry> response = new ArrayList<DictionaryEntry>();
		if (query.length() < 1) {
			return response;
		}
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        /*TODO: query terms also have to be processes with the analyser before they can be compared with the keys of
         of the inverted list - verify if TermQuery does so automatically or if we have to do this ourselves*/
        Query q = new BooleanQuery();

        //TODO: use query parse instead of a single TermQuery to enable boolean operators (AND, OR, NOT, etc.)
        Query query1 = new TermQuery(new Term("Content", query));
        ((BooleanQuery) q).add(query1, BooleanClause.Occur.MUST);
		if (!language.equals(Language.ALL)) {
            int languageId = Utils$.MODULE$.lang2Byte(language);
            //TODO: check if it's really correct and sound to define the @code{precisionStep} here
            Query query2 = NumericRangeQuery.newIntRange("Lang", 1, languageId, languageId, true, true);
            ((BooleanQuery) q).add(query2, BooleanClause.Occur.MUST);
		}


		IndexReader reader = IndexReader.open(directoryIndex);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		logger.info("Hits: " + hits.length);
		for (ScoreDoc hit : hits) {
			Document d = searcher.doc(hit.doc);
			int dbId = (int) d.getField("DbId").numericValue();
			Optional<DictionaryEntry> entry = DictionaryEntry.fetchById(dbId);
			if (entry.isPresent()) {
				response.add(entry.get());
			}
		}

		return response;
	}

    public static void main(String[] args) {
        try {
            new Indexer().createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package de.fusionfactory.index_vivus.indexer;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.services.Language;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCount;
import de.fusionfactory.index_vivus.services.scalaimpl.DictionaryEntryListWithTotalCount$;
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
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:23
 */
public class Indexer {
	private Tokenizer tokenizer;
	private File fsDirectoryFile = new File(LocationProvider.getInstance().getDataDir(), "lucene_index");
	private Directory directoryIndex;
	private Logger logger;

    public Indexer() {
		tokenizer = new Tokenizer();
		logger = Logger.getLogger(this.getClass());
		logger.info(fsDirectoryFile.getAbsolutePath());

		try {
			directoryIndex = new SimpleFSDirectory(fsDirectoryFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ensureIndexCreated() throws IOException {
		if (fsDirectoryFile.isDirectory()) {
			logger.info("FSDirectory exists, use it O_o.");
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

		indexWriter.close();
	}

	private void insertDocument(IndexWriter w, DictionaryEntry entry) throws IOException {
		int dbId = entry.getId(), lang = entry.sourceLanguage();

		List<String> tokens = tokenizer.getTokenizedString(entry.getDescription());
		String content = Tokenizer.implodeArray(tokens.toArray(new String[tokens.size()]), " ");

		Document document = new Document();
		document.add(new IntField("DbId", dbId, Field.Store.YES));
		document.add(new IntField("Lang", lang, Field.Store.YES));
		document.add(new TextField("Content", content, Field.Store.NO));

		w.addDocument(document);
	}

	/*public List<DictionaryEntry> getSearchResults(String query) throws IOException, ParseException {
		return getSearchResults(query, Language.ALL);
	}*/

	public DictionaryEntryListWithTotalCount getSearchResults(String query, Language language, int hitsPerPage, int offset) throws ParseException, IOException {
		logger.warn(String.format("query=%s,lang=%s,hitspp=%d,offset=%d", query, language, hitsPerPage, offset));
        List<DictionaryEntry> response = new ArrayList<DictionaryEntry>();
		if (query.length() < 1) {
			return DictionaryEntryListWithTotalCount$.MODULE$.apply(response, response.size());
		}
		Query q = new BooleanQuery();

		Query query1 = new TermQuery(new Term("Content", query));
		((BooleanQuery) q).add(query1, BooleanClause.Occur.MUST);
		if (!language.equals(Language.ALL)) {
			Query query2 = NumericRangeQuery.newIntRange("Lang", 1, (int) Utils$.MODULE$.lang2Byte(language), (int) Utils$.MODULE$.lang2Byte(language), true, true);
			((BooleanQuery) q).add(query2, BooleanClause.Occur.MUST);
		}


		IndexReader reader = DirectoryReader.open(directoryIndex);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        TotalHitCountCollector counter = new TotalHitCountCollector();

		searcher.search(q, collector);
        searcher.search(q, counter);

        ScoreDoc[] hits = collector.topDocs(offset, hitsPerPage).scoreDocs;
		logger.info("Hits: " + hits.length);
		for (ScoreDoc hit : hits) {
			Document d = searcher.doc(hit.doc);
			int dbId = (int) d.getField("DbId").numericValue();
			Optional<DictionaryEntry> entry = DictionaryEntry.fetchById(dbId);
			if (entry.isPresent()) {
				response.add(entry.get());
			}
		}

		return DictionaryEntryListWithTotalCount$.MODULE$.apply(response, counter.getTotalHits());
	}
}

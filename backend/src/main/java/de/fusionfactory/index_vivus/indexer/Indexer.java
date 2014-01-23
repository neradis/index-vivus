package de.fusionfactory.index_vivus.indexer;

import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.tokenizer.Tokenizer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 19:23
 */
public class Indexer {
	private Directory directoryIndex = new RAMDirectory();
	private Tokenizer tokenizer;
	private File fsDirectoryFile = new File(LocationProvider.getInstance().getDataDir().getPath(), "index.lucene.bin");
	private Logger logger;

	public Indexer() {
		tokenizer = new Tokenizer();
		logger = Logger.getLogger(this.getClass());
		logger.info(fsDirectoryFile.getAbsolutePath());
	}

	public void mapIndexToRam() throws IOException {
		if (fsDirectoryFile.exists()) {
			Directory fsDirectory = FSDirectory.open(fsDirectoryFile);
			StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, standardAnalyzer);
			IndexWriter indexWriter = new IndexWriter(directoryIndex, config);
			indexWriter.addIndexes(fsDirectory);
		} else {
			createIndex();
		}
	}

	private void createIndex() throws IOException {
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_46);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, standardAnalyzer);
		IndexWriter indexWriter = new IndexWriter(directoryIndex, config);

		List<DictionaryEntry> dictionaryEntryList = DictionaryEntry.fetchAll(null);
		for (DictionaryEntry e : dictionaryEntryList) {
			insertDocument(indexWriter, e);
		}

		if (!fsDirectoryFile.exists()) {
			fsDirectoryFile.createNewFile();
		}

		{
			Directory fsDirectory = FSDirectory.open(fsDirectoryFile);
			IndexWriterConfig fsConfig = new IndexWriterConfig(Version.LUCENE_46, standardAnalyzer);
			IndexWriter fsIndexWriter = new IndexWriter(fsDirectory, fsConfig);
			fsIndexWriter.addIndexes(directoryIndex);
			fsIndexWriter.close();
		}
	}

	private void insertDocument(IndexWriter w, DictionaryEntry entry) throws IOException {
		int dbId = entry.getId(), lang = 0;

		List<String> tokens = tokenizer.getTokenizedString(entry.getDescription());
		String content = Tokenizer.implodeArray(tokens.toArray(new String[tokens.size()]), " ");

		Document document = new Document();
		document.add(new IntField("DbId", dbId, Field.Store.YES));
		document.add(new IntField("Lang", lang, Field.Store.YES));
		document.add(new TextField("Content", content, Field.Store.NO));

		w.addDocument(document);
	}
}

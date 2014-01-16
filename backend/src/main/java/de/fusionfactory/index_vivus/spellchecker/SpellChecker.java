package de.fusionfactory.index_vivus.spellchecker;

import com.aliasi.io.FileLineReader;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.google.common.io.Resources;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 03.12.13
 * Time: 18:02
 */
public class SpellChecker {
    private static final String WORDLIST = Resources.getResource("spellchecker/top10000de.txt").getPath();
    private static final String MODEL_FILE = "spellchecker.model";
    private static final double MATCH_WEIGHT = -0.0;
    private static final double DELETE_WEIGHT = -4.0;
    private static final double INSERT_WEIGHT = -1.0;
    private static final double SUBSTITUTE_WEIGHT = -2.0;
    private static final double TRANSPOSE_WEIGHT = -2.0;
    private static final int NGRAM_LENGTH = 5;
    private CompiledSpellChecker spellCheckerIndex = null;
    private AutoCompleter autoCompleter;

    public SpellChecker() {
        // read our index to be ready for spellchecking :o
        try {
			createIndex();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }

        FixedWeightEditDistance fixedEdit = new FixedWeightEditDistance(MATCH_WEIGHT, DELETE_WEIGHT, INSERT_WEIGHT, SUBSTITUTE_WEIGHT, TRANSPOSE_WEIGHT);
        try {
            autoCompleter = new AutoCompleter(createAutocompletionIndex(), fixedEdit, 5, 10000, -25.0);
        } catch (IOException e) {
        }
    }

    /**
     * Creates our index for spellchecking
     *
     * @throws IOException
     */
    public void createIndex() throws IOException, ClassNotFoundException {
        FixedWeightEditDistance fixedEdit = new FixedWeightEditDistance(MATCH_WEIGHT, DELETE_WEIGHT, INSERT_WEIGHT, SUBSTITUTE_WEIGHT, TRANSPOSE_WEIGHT);
        NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
        TrainSpellChecker tsc = new TrainSpellChecker(lm, fixedEdit);

        InputStream fis;
        BufferedReader br;
        String line;

        fis = new FileInputStream(WORDLIST);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        while ((line = br.readLine()) != null) {
            tsc.handle(line);
        }

        FileOutputStream fos;
        BufferedOutputStream bos;
        ObjectOutputStream oos;

        fos = new FileOutputStream(MODEL_FILE);
        bos = new BufferedOutputStream(fos);
        oos = new ObjectOutputStream(bos);

        tsc.compileTo(oos);

        Streams.closeQuietly(oos);
        Streams.closeQuietly(bos);
        Streams.closeQuietly(fos);

        readIndex();
    }

    /**
     * reads our index file and store it in an CompiledSpellChecker container
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readIndex() throws IOException, ClassNotFoundException {
        FileInputStream fis;
        BufferedInputStream bis;
        ObjectInputStream ois;

        fis = new FileInputStream(MODEL_FILE);
        bis = new BufferedInputStream(fis);
        ois = new ObjectInputStream(bis);

        spellCheckerIndex = (CompiledSpellChecker) ois.readObject();

        Streams.closeQuietly(ois);
        Streams.closeQuietly(bis);
        Streams.closeQuietly(fis);
    }

    /**
     * returns the best alternative of given keyword
     *
     * @param keyword
     * @return
     * @throws SpellCheckerException
     */
    public String getAlternativeWords(String keyword) throws SpellCheckerException {
        if (spellCheckerIndex == null) {
            throw new SpellCheckerException("no index defined");
        }

        String alternative = spellCheckerIndex.didYouMean(keyword);

        return alternative;
    }

    /**
     * creates an simple Autocompletion Index
     *
     * @return
     * @throws IOException
     * @todo for the count object we should use the request frequency for given keywords, in this example we use the word size for weighting
     */
    public Map<String, Float> createAutocompletionIndex() throws IOException {
        Map<String, Float> m = new HashMap<String, Float>();
        File wordList = new File(WORDLIST);
        String[] lines = FileLineReader.readLineArray(wordList, "UTF-8");
        for (String line : lines) {
            m.put(line, (float) line.length());
        }

        return m;
    }

    /**
     * @param prefix
     * @return
     */
    public String[] getAutocompleteSuggestions(String prefix) {
        SortedSet<ScoredObject<String>> completions = autoCompleter.complete(prefix);

        String[] r = new String[completions.size()];
        int i = 0;
        for (ScoredObject<String> so : completions) {
            r[i++] = so.getObject();
        }

        return r;
    }
}
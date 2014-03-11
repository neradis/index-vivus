package de.fusionfactory.index_vivus.spellchecker;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;
import scala.slick.session.Session;

import java.io.*;
import java.util.*;

import static java.lang.String.format;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 03.12.13
 * Time: 18:02
 */
public class SpellChecker {
    private static Logger logger = Logger.getLogger(SpellChecker.class);

    public static final double MIN_SCORE = -25.0;
    private static final double MATCH_WEIGHT = -0.0;
    private static final double DELETE_WEIGHT = -4.0;
    private static final double INSERT_WEIGHT = -1.0;
    private static final double SUBSTITUTE_WEIGHT = -2.0;
    private static final double TRANSPOSE_WEIGHT = -2.0;
    private static final int NGRAM_LENGTH = 5;
    private static final EnumSet<Language> ILLEGAL_LANGUAGE_PARAMS = EnumSet.of(Language.ALL, Language.NONE);

    protected volatile Optional<CompiledSpellChecker> spellCheckerPromise = Optional.absent();
    protected volatile Optional<AutoCompleter> autoCompleterPromise = Optional.absent();

    public final Language language;

    public SpellChecker(Language language) {

        if (ILLEGAL_LANGUAGE_PARAMS.contains(language)) {
            throw new IllegalArgumentException(format("invalid language %s"));
        }
        this.language = language;

        logger.debug("Option state: " + spellCheckerPromise + " " + autoCompleterPromise);
        new SpellCheckerAsyncLoading(this);
    }

    /**
     * Creates our index for spellchecking
     *
     * @throws IOException
     */
    protected void createIndex(Set<String> keywords) throws IOException, ClassNotFoundException {
        NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
        TrainSpellChecker tsc = new TrainSpellChecker(lm, fixedWeightEditDistance());

        for (String kw : keywords) {
            tsc.handle(kw);
        }

        FileOutputStream fos = new FileOutputStream(spellcheckerModelPath());
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        tsc.compileTo(oos);

        Streams.closeQuietly(oos);
        Streams.closeQuietly(bos);
        Streams.closeQuietly(fos);
    }

    private FixedWeightEditDistance fixedWeightEditDistance() {
        return new FixedWeightEditDistance(MATCH_WEIGHT, DELETE_WEIGHT, INSERT_WEIGHT, SUBSTITUTE_WEIGHT, TRANSPOSE_WEIGHT);
    }

    /**
     * reads the spell checker model
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected CompiledSpellChecker provideCompiledSpellCheckerModel(Set<String> keywords) {

        ensureCompiledSpellChecker(keywords);

        CompiledSpellChecker spellChecker;
        try {
            FileInputStream fis = new FileInputStream(spellcheckerModelPath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);

            spellChecker = (CompiledSpellChecker) ois.readObject();

            Streams.closeQuietly(ois);
            Streams.closeQuietly(bis);
            Streams.closeQuietly(fis);
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException("Error loading the spellchecker model", ex);
        }
        return spellChecker;
    }

    protected synchronized void ensureCompiledSpellChecker(Set<String> keywords) {
        if (!spellcheckerModelPath().isFile()) {
            try {
                createIndex(keywords);
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Error creating spellchecker index", ex);
            }

        }
    }

    protected AutoCompleter createAutoCompleter(Set<String> keywords) {

        AutoCompleter autoCompleter;
            Map<String, Float> tokenMap = Maps.newHashMapWithExpectedSize(keywords.size());

            for (String kw : keywords) {
                tokenMap.put(kw, 1f);
            }

            if (tokenMap.isEmpty()) {
                throw new IllegalStateException("no known keywords?");
            }

            autoCompleter = new AutoCompleter(tokenMap, fixedWeightEditDistance(), 8, 10000, MIN_SCORE);
        logger.debug("auto completer created");
        return autoCompleter;
    }

    public boolean alternativesReady() {
        return spellCheckerPromise.isPresent();
    }

    /**
     * returns the best alternative of given keyword
     *
     * @param keyword
     * @return
     * @throws SpellCheckerException
     */
    public String getBestAlternativeWord(String keyword) throws SpellCheckerException {

        if (spellCheckerPromise.isPresent()) {
            String alternative = spellCheckerPromise.get().didYouMean(keyword);
            return alternative != null ? alternative : keyword;
        }
        logger.warn(format("spellchecker not ready - returning '%s' uncorrected", keyword));
        return keyword;
    }

    public boolean autoCompletionReady() {
        return autoCompleterPromise.isPresent();
    }

    /**
     * @param prefix
     * @return
     */
    public List<String> getAutocompleteSuggestions(String prefix) {

        if (autoCompleterPromise.isPresent()) {
            SortedSet<ScoredObject<String>> completions = autoCompleterPromise.get().complete(prefix);

            logger.debug(format("autocompleter was ready and gave %d completions for %s", completions.size(), prefix));

            List<String> completionsList = new ArrayList<>(completions.size());
            for (ScoredObject<String> so : completions) {
                completionsList.add(so.getObject());
            }

            return completionsList;
        } else {
            logger.warn("autocompleter not ready - returning empty list");
            return ImmutableList.of();
        }
    }

    protected Set<String> fetchKeywords() {
        logger.debug("Acquiring lock to fetch all keywords");

        List<String> keywords = DbHelper.transaction(new DbHelper.Operations<List<String>>() {
            @Override
            public List<String> perform(Session tx) {
                logger.debug("Lock to fetch all keywords acquired");
                List<String> kwList = DictionaryEntry.fetchKeywordsForLanguage(language, tx);
                logger.debug(format("%d keywords retrieved as list for models", kwList.size()));
                return kwList;
            }
        });
        Set<String> entrySet = Sets.newHashSetWithExpectedSize(keywords.size());
        entrySet.addAll(keywords);
        logger.debug(format("%d keywords for models after dupcliate removal", entrySet.size()));
        return entrySet;
    }

    protected File spellcheckerModelPath() {
        return new File(LocationProvider.getInstance().getDataDir(), languageFilename("spellchecking"));
    }

    protected String languageFilename(String type) {
        return String.format("%s_%s.model", language.name(), type);
    }
}

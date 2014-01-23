package de.fusionfactory.index_vivus.spellchecker;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.fusionfactory.index_vivus.configuration.LocationProvider;
import de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry;
import de.fusionfactory.index_vivus.persistence.DbHelper;
import de.fusionfactory.index_vivus.services.Language;
import org.apache.log4j.Logger;
import scala.slick.session.Session;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.String.format;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 03.12.13
 * Time: 18:02
 */
public class SpellChecker {
    public static final double MIN_SCORE = -25.0;
    private static final double MATCH_WEIGHT = -0.0;
    private static final double DELETE_WEIGHT = -4.0;
    private static final double INSERT_WEIGHT = -1.0;
    private static final double SUBSTITUTE_WEIGHT = -2.0;
    private static final double TRANSPOSE_WEIGHT = -2.0;
    private static final int NGRAM_LENGTH = 5;
    private static final EnumSet<Language> ILLEGAL_LANGUAGES = EnumSet.of(Language.ALL, Language.NONE);
    public static final String INIT_PENDING_DEFAULT_ALTERNATIVE = "domus";
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Future<Set<String>> keywordsFuture = executor.submit(
            new Callable<Set<String>>() {
                @Override
                public Set<String> call() throws Exception {
                    return fetchKeywords();
                }
            });
    private final Future<CompiledSpellChecker> spellCheckerFuture = executor.submit(
            new Callable<CompiledSpellChecker>() {
                @Override
                public CompiledSpellChecker call() throws Exception {
                    return provideCompiledSpellCheckerModel(keywordsFuture);
                }
            });
    private final Future<AutoCompleter> autoCompleterFuture = executor.submit(
            new Callable<AutoCompleter>() {
                @Override
                public AutoCompleter call() throws Exception {
                    return createAutoCompleter(keywordsFuture);
                }
            });
    Logger logger = Logger.getLogger(SpellChecker.class);
    private Language language;

    public SpellChecker(Language language) {

        if (ILLEGAL_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException(format("invalid language %s"));
        }
        this.language = language;
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
    private CompiledSpellChecker provideCompiledSpellCheckerModel(Future<Set<String>> keywordsFuture) {

        ensureCompiledSpellChecker(keywordsFuture);


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

    private synchronized void ensureCompiledSpellChecker(Future<Set<String>> keywordsFuture) {
        if (!spellcheckerModelPath().isFile()) {
            try {
                createIndex(keywordsFuture.get());
            } catch (ExecutionException | InterruptedException | IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Error creating spellchecker index", ex);
            }

        }
    }

    protected AutoCompleter createAutoCompleter(Future<Set<String>> keywordsFuture) {

        AutoCompleter autoCompleter = null;
        try {
            Set<String> keywords = keywordsFuture.get();
            Map<String, Float> tokenMap = Maps.newHashMapWithExpectedSize(keywords.size());

            for (String kw : keywords) {
                tokenMap.put(kw, 1f);
            }
            autoCompleter = new AutoCompleter(tokenMap, fixedWeightEditDistance(), 5, 10000, MIN_SCORE);
        } catch (ExecutionException | InterruptedException ex) {
            throw new IllegalStateException("Error creating autocompleter", ex);
        }
        logger.debug("auto completer created");
        return autoCompleter;
    }

    /**
     * returns the best alternative of given keyword
     *
     * @param keyword
     * @return
     * @throws SpellCheckerException
     */
    public String getBestAlternativeWord(String keyword) throws SpellCheckerException {

        if (spellCheckerFuture.isDone() && !spellCheckerFuture.isCancelled()) {
            String alternative;
            try {
                alternative = spellCheckerFuture.get().didYouMean(keyword);
            } catch (InterruptedException | ExecutionException ex) {
                throw new IllegalStateException("error retrieving spellchecker", ex);
            }

            return alternative != null ? alternative : keyword;
        }
        logger.warn(format("spellchecker not ready - returning '%s' as default value",
                INIT_PENDING_DEFAULT_ALTERNATIVE));
        return INIT_PENDING_DEFAULT_ALTERNATIVE;
    }

    protected Optional<AutoCompleter> getAutoCompleter() {
        try {
            return Optional.of(autoCompleterFuture.get(250, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalArgumentException("error waiting for autocompleter", ex);
        } catch (TimeoutException e) {
            return Optional.absent();
        }
    }

    /**
     * @param prefix
     * @return
     */
    public List<String> getAutocompleteSuggestions(String prefix) {
        final Optional<AutoCompleter> acOpt = getAutoCompleter();
        if (acOpt.isPresent()) {
            SortedSet<ScoredObject<String>> completions = acOpt.get().complete(prefix);

            logger.debug(format("autocompleter was ready and out $d completions for $s", completions.size(), prefix));

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
        List<DictionaryEntry> entries = DbHelper.transaction(new DbHelper.Operations<List<DictionaryEntry>>() {
            @Override
            public List<DictionaryEntry> perform(Session tx) {
                List<DictionaryEntry> ds = DictionaryEntry.fetchBySourceLanguage(language, tx);
                logger.debug(format("%d entries retrieved as list for models", ds.size()));
                return ds;
            }
        });
        Set<String> entrySet = Sets.newHashSet(Lists.transform(entries, new Function<DictionaryEntry, String>() {
            @Nullable
            @Override
            public String apply(@Nullable DictionaryEntry input) {
                return input.getKeyword();
            }
        }));
        logger.debug(format("%d keywords retrieved as set for models", entrySet.size()));
        return entrySet;
    }

    protected File spellcheckerModelPath() {
        return new File(LocationProvider.getInstance().getDataDir(), languageFilename("spellchecking"));
    }

    protected String languageFilename(String type) {
        return String.format("%s_%s.model", language.name(), type);
    }
}

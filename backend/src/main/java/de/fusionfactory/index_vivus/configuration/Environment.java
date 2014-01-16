package de.fusionfactory.index_vivus.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.fusionfactory.index_vivus.configuration.impl.ScalaImpl;

import java.util.Collection;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public enum Environment {

    DEVELOPMENT("development", "dev"),
    TEST("test", "test"),
    PRODUCTION("production", "prod");

    Environment(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    String name;
    String shortName;


    public static final Environment DEFAULT_ENVIRONMENT = DEVELOPMENT;
    private static Optional<Environment> activeEnvironment = Optional.absent();

    private static ImmutableMap<String, Environment> prefix2Env = ImmutableMap.of(
            "dev", DEVELOPMENT,
            "test", TEST,
            "prod", PRODUCTION
    );

    public static Environment byString(final String str) {
        Collection<String> matches = Collections2.filter(prefix2Env.keySet(), new Predicate<String>() {

            @Override
            public boolean apply(String envDesc) {
                return str.startsWith(envDesc);
            }
        });

        if(matches.isEmpty()) {
            throw new UnrecognizedEnvironmentException(str);
        } else {
            Environment envCandidate = prefix2Env.get(matches.iterator().next());
            if(envCandidate.name.startsWith(str)) {
                return envCandidate;
            } else {
                throw new UnrecognizedEnvironmentException(str);
            }
        }
    }

    public static Environment getActive() {
        return ScalaImpl.getActiveEnvironment(ImmutableList.of("RAILS_ENV", "ENV"));
    }

    static class UnrecognizedEnvironmentException extends RuntimeException {

        UnrecognizedEnvironmentException(String unmatchedEnvName) {
            super(String.format("No environment machtes '%s'", unmatchedEnvName));
        }
    }


    public static class AmbiguousEnvironmentException extends RuntimeException {

        public AmbiguousEnvironmentException(String... unmatchedEnvNames) {
            super(String.format("Found multiple non-identical enviroment requests: '%s'",
                  Joiner.on(", ").join(unmatchedEnvNames)));
        }
    }
}

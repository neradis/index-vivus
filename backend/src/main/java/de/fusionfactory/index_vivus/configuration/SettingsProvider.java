package de.fusionfactory.index_vivus.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class SettingsProvider {

    private static Optional<SettingsProvider> activeSettings = Optional.absent();

    private static ImmutableMap<Environment, SettingsProvider> env2Settings = ImmutableMap.of(
            Environment.DEVELOPMENT, new DevelopmentSettingProvider(),
            Environment.TEST, new TestSettingsProvider(),
            Environment.PRODUCTION, new ProductionSettingsProvider()
    );

    public static SettingsProvider getInstance() {
        if( !activeSettings.isPresent() ) {
            activeSettings = Optional.of(env2Settings.get(Environment.getActive()));
        }
        return activeSettings.get();
    }

    abstract public String getDatabaseUrl();


    public final static String H2_MEM_DB_OPTIONS =
            Joiner.on(',').join(ImmutableList.of("DB_CLOSE_DELAY=-1"));

    public final static String H2_FILE_DB_OPTIONS =
            Joiner.on(';').join(ImmutableList.of("AUTO_SERVER=TRUE", "AUTO_SERVER_PORT=6543"));


    public static class DevelopmentSettingProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return "jdbc:h2:mem:index_vivus_dev;" + H2_MEM_DB_OPTIONS;
        }
    }

    public static class TestSettingsProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return buildFileDbUrl("index_vivus_test");
        }
    }

    public static class ProductionSettingsProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return buildFileDbUrl("index_vivus_prod");
        }
    }

    private static String buildFileDbUrl(String pathSuffix) {
        File dbFile = new File(LocationProvider.getInstance().getDataDir(), pathSuffix);
        try {
            return String.format("jdbc:h2:file:%s;%s", dbFile.getCanonicalPath(), H2_FILE_DB_OPTIONS);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

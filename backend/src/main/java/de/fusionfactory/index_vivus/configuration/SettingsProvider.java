package de.fusionfactory.index_vivus.configuration;

import com.google.common.base.Optional;
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

    public static class DevelopmentSettingProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return "jdbc:h2:mem:index_vivus_dev";
        }
    }

    public static class TestSettingsProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return buildDbFilePath("index_vivus_test");
        }
    }

    public static class ProductionSettingsProvider extends SettingsProvider {

        @Override
        public String getDatabaseUrl() {
            return buildDbFilePath("index_vivus_prod");
        }
    }

    private static String buildDbFilePath(String urlSuffix) {
        File dbFile = new File(LocationProvider.INSTANCE.getBackendRoot(), urlSuffix);
        try {
            return String.format("jdbc:h2:file:%s", dbFile.getCanonicalPath());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

package de.fusionfactory.index_vivus.configuration;

import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class LocationProvider {
    protected static final LocationProviderImpl impl = new LocationProviderImpl();
    public static final LocationProvider INSTANCE = new LocationProvider();

    protected LocationProvider() {
    }

    public static LocationProvider getInstance() {
        return INSTANCE;
    }

    public File getProjectRoot() {
        return impl.getProjectRoot();
    }

    public File getProjectBuild() {
        return impl.getProjectBuild();
    }

    public File getBackendRoot() {
        return impl.getBackendRoot();
    }

    public File getBackendBuild() {
        return impl.getBackendBuild();
    }

    public File getDataDir() {
        return impl.getDataDir();
    }

    public File getInputDir() {
        return impl.getInputDir();
    }

    public File getDictionaryDir() {
        return impl.getDictionaryDir();
    }
    
    public static void main(String[] args) {
        System.out.println(
            ImmutableList.of(INSTANCE.getProjectRoot(), INSTANCE.getProjectBuild(),
                    INSTANCE.getBackendRoot(), INSTANCE.getBackendBuild()));
    }
}

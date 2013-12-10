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


    public static void main(String[] args) {
        System.out.println(
            ImmutableList.of(INSTANCE.getProjectRoot(), INSTANCE.getProjectBuild(),
                    INSTANCE.getBackendRoot(), INSTANCE.getBackendBuild()));


    }
}

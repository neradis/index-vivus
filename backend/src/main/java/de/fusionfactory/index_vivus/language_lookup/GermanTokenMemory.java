package de.fusionfactory.index_vivus.language_lookup;

import com.google.common.base.Optional;
import de.fusionfactory.index_vivus.language_lookup.scalaimpl.GermanTokenMemoryImpl;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
 public class GermanTokenMemory {

    protected static final GermanTokenMemoryImpl impl = new GermanTokenMemoryImpl();
    public static final GermanTokenMemory INSTANCE = new GermanTokenMemory();

    public static GermanTokenMemory getInstance() {
        return INSTANCE;
    }

    public Optional<Boolean> isGerman(String token) {
        return impl.isGerman(token);
    }

    public boolean hasResult(String token) {
        return  impl.hasResult(token);
    }

    public void put(String token, boolean isGerman) {
        impl.put(token, isGerman);
    }
}

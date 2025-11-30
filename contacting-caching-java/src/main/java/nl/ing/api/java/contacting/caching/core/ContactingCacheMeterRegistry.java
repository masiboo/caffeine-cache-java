package nl.ing.api.java.contacting.caching.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import java.util.HashSet;
import java.util.Set;

public final class ContactingCacheMeterRegistry {

    private static final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();

    private ContactingCacheMeterRegistry() {}

    public static void add(MeterRegistry registry) {
        compositeMeterRegistry.add(registry);
    }

    public static Set<MeterRegistry> getRegistries() {
        return new HashSet<>(compositeMeterRegistry.getRegistries());
    }
}
package nl.ing.api.java.contacting.caching.core;


import com.ing.apisdk.toolkit.connectivity.api.LocalAwareDelegatingExecutorService;
import nl.ing.api.java.contacting.caching.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ContactingCacheExecutor {

    private static final Logger log = LoggerFactory.getLogger(ContactingCacheExecutor.class);

    private static final ExecutorService executor;

    static {
        int poolSize = Math.max(Runtime.getRuntime().availableProcessors() - 1, 4);
        executor = new LocalAwareDelegatingExecutorService(
                Executors.newFixedThreadPool(poolSize, new NamedThreadFactory("caching"))
        );
        log.debug("Initialized ContactingCache executor with pool size {}", poolSize);
    }

    private ContactingCacheExecutor() {}

    public static ExecutorService executor() {
        return executor;
    }
}
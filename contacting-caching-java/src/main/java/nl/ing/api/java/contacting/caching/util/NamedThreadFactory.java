package nl.ing.api.java.contacting.caching.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory that names threads with a given prefix and sequence number, and sets them as daemon.
 */
public final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        int seq = sequence.getAndIncrement();
        String threadName = prefix + (seq > 1 ? "-" + seq : "");
        Thread thread = new Thread(runnable, threadName);
        thread.setDaemon(true);
        return thread;
    }
}

package org.kohsuke.github;

import org.kohsuke.github.function.SupplierThrows;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * GitHubSanityCachedValue limits queries for a particular value to once per second.
 */
class GitHubSanityCachedValue<T> {

    private long lastQueriedAtEpochSeconds = 0;
    private T lastResult = null;
    // Allow concurrent readers while a refresh is not needed.
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    /**
     * Gets the value from the cache or calls the supplier if the cache is empty or out of date.
     *
     * @param query
     *            a supplier the returns an updated value. Only called if the cache is empty or out of date.
     * @return the value from the cache or the value returned from the supplier.
     * @throws E
     *             the exception thrown by the supplier if it fails.
     */
    <E extends Throwable> T get(SupplierThrows<T, E> query) throws E {
        readLock.lock();
        try {
            if (Instant.now().getEpochSecond() <= lastQueriedAtEpochSeconds) {
                return lastResult;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (Instant.now().getEpochSecond() > lastQueriedAtEpochSeconds) {
                lastResult = query.get();
                lastQueriedAtEpochSeconds = Instant.now().getEpochSecond();
            }
            return lastResult;
        } finally {
            writeLock.unlock();
        }
    }
}

package org.kohsuke.github;

import org.kohsuke.github.function.SupplierThrows;

import java.time.Instant;
import java.util.function.Function;

/**
 * GitHubSanityCachedValue limits queries for a particular value to once per second.
 */
class GitHubSanityCachedValue<T> {

    private final Object lock = new Object();
    private long lastQueriedAtEpochSeconds = 0;
    private T lastResult = null;

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
        return get((value) -> Boolean.FALSE, query);
    }

    /**
     * Gets the value from the cache or calls the supplier if the cache is empty or out of date.
     *
     * @param isExpired
     *            a supplier that returns true if the cached value is no longer valid.
     * @param query
     *            a supplier the returns an updated value. Only called if the cache is empty or out of date.
     * @return the value from the cache or the value returned from the supplier.
     * @throws E
     *             the exception thrown by the supplier if it fails.
     */
    <E extends Throwable> T get(Function<T, Boolean> isExpired, SupplierThrows<T, E> query) throws E {
        synchronized (lock) {
            if (Instant.now().getEpochSecond() > lastQueriedAtEpochSeconds || isExpired.apply(lastResult)) {
                lastResult = query.get();
                lastQueriedAtEpochSeconds = Instant.now().getEpochSecond();
            }
        }
        return lastResult;
    }
}

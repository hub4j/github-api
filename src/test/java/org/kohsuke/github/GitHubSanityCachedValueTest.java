package org.kohsuke.github;

import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * The Class GitHubSanityCachedValueTest.
 */
public class GitHubSanityCachedValueTest {

    private static void alignToStartOfSecond() {
        while (Instant.now().getNano() > 100_000_000) {
            Thread.yield();
        }
    }

    /**
     * Tests that the cache returns the same value without querying again when accessed multiple times within the same
     * second.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void cachesWithinSameSecond() throws Exception {
        alignToStartOfSecond();
        GitHubSanityCachedValue<String> cachedValue = new GitHubSanityCachedValue<>();
        AtomicInteger calls = new AtomicInteger();

        String first = cachedValue.get(() -> {
            calls.incrementAndGet();
            return "value";
        });
        String second = cachedValue.get(() -> {
            calls.incrementAndGet();
            return "value";
        });

        assertThat(first, equalTo("value"));
        assertThat(second, equalTo("value"));
        assertThat(calls.get(), equalTo(1));
    }

    /**
     * Tests that multiple concurrent callers only trigger a single refresh of the cached value, preventing redundant
     * queries.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void concurrentCallersOnlyRefreshOnce() throws Exception {
        alignToStartOfSecond();
        GitHubSanityCachedValue<String> cachedValue = new GitHubSanityCachedValue<>();
        AtomicInteger calls = new AtomicInteger();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(5);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    ready.countDown();
                    start.await();
                    String value = cachedValue.get(() -> {
                        calls.incrementAndGet();
                        return "value";
                    });
                    results.add(value);
                } catch (Exception ignored) {
                    results.add(null);
                } finally {
                    finished.countDown();
                }
            });
            thread.start();
        }

        ready.await();
        start.countDown();
        finished.await();

        assertThat(calls.get(), equalTo(1));
        assertThat(results.size(), equalTo(5));
        for (String result : results) {
            assertThat(result, notNullValue());
            assertThat(result, equalTo("value"));
        }
    }

    /**
     * Tests that a result which is already expired on arrival — for example, the {@code GHRateLimit.UnknownLimitRecord}
     * returned when a GitHub Enterprise {@code /rate_limit} endpoint responds with 404 — is still held for one second.
     * Without the time-based TTL, re-checking expiry immediately after a refresh would cause every subsequent call to
     * re-query, creating a query storm.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void doesNotReQueryWhenResultIsAlreadyExpiredOnArrival() throws Exception {
        alignToStartOfSecond();
        GitHubSanityCachedValue<String> cachedValue = new GitHubSanityCachedValue<>();
        AtomicInteger calls = new AtomicInteger();

        // Supplier always returns a value that an isExpired() check would immediately reject,
        // e.g. GHRateLimit.UnknownLimitRecord when GitHub Enterprise returns 404 for /rate_limit.
        cachedValue.get(() -> {
            calls.incrementAndGet();
            return "expired-on-arrival";
        });
        cachedValue.get(() -> {
            calls.incrementAndGet();
            return "expired-on-arrival";
        });
        cachedValue.get(() -> {
            calls.incrementAndGet();
            return "expired-on-arrival";
        });

        assertThat(calls.get(), equalTo(1));
    }

    /**
     * Tests that the cache is refreshed after one second has elapsed, triggering a new query to retrieve the updated
     * value.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void refreshesAfterOneSecond() throws Exception {
        GitHubSanityCachedValue<String> cachedValue = new GitHubSanityCachedValue<>();
        AtomicInteger calls = new AtomicInteger();

        String first = cachedValue.get(() -> {
            calls.incrementAndGet();
            return "value";
        });

        Thread.sleep(1100);

        String second = cachedValue.get(() -> {
            calls.incrementAndGet();
            return "value";
        });

        assertThat(first, equalTo("value"));
        assertThat(second, equalTo("value"));
        assertThat(calls.get(), equalTo(2));
    }
}

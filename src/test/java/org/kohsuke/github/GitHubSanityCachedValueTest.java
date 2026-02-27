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
                    String value = cachedValue.get((result) -> result == null, () -> {
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

package org.kohsuke.github;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A GitHub API Rate Limit Checker called before each request
 * <p>
 * GitHub allots a certain number of requests to each user or application per period of time (usually per hour). The
 * number of requests remaining is returned in the response header and can also be requested using
 * {@link GitHub#getRateLimit()}. This requests per interval is referred to as the "rate limit".
 * </p>
 * <p>
 * GitHub prefers that clients stop before exceeding their rate limit rather than stopping after they exceed it. The
 * {@link RateLimitChecker} is called before each request to check the rate limit and wait if the checker criteria are
 * met.
 * </p>
 * <p>
 * Checking your rate limit using {@link GitHub#getRateLimit()} does not effect your rate limit, but each {@link GitHub}
 * instance will attempt to cache and reuse the last see rate limit rather than making a new request.
 * </p>
 */
public abstract class RateLimitChecker {

    private static final Logger LOGGER = Logger.getLogger(RateLimitChecker.class.getName());

    public static final RateLimitChecker NONE = new RateLimitChecker() {
    };

    /**
     * Decides whether the current request exceeds the allowed "rate limit" budget. If this determines the rate limit
     * will be exceeded, this method should sleep for some amount of time and must return {@code true}. Implementers are
     * free to choose whatever strategy they prefer for what is considered to exceed the budget and how long to sleep.
     *
     * <p>
     * The caller of this method figures out which {@link GHRateLimit.Record} applies for the current request add
     * provides it to this method.
     * </p>
     * <p>
     * It is important to remember that rate limit reset times are only accurate to the second. Trying to sleep to
     * exactly the reset time would be likely to produce worse behavior rather than better. For this reason
     * {@link GitHubRateLimitChecker} may choose to add more sleep times when a checker indicates the rate limit was
     * exceeded.
     * </p>
     * <p>
     * As long as this method returns {@code true} it is guaranteed that {@link GitHubRateLimitChecker} will get updated
     * rate limit information and call this method again with {@code count} incremented by one. After this method
     * returns {@code true} at least once, the calling {@link GitHubRateLimitChecker} may choose to wait some additional
     * period of time between calls to this checker.
     * </p>
     * <p>
     * After this checker returns {@code false}, the calling {@link GitHubRateLimitChecker} will let the request
     * continue. If this method returned {@code true} at least once for a particular request, the calling
     * {@link GitHubRateLimitChecker} may choose to wait some additional period of time before letting the request be
     * sent.
     * </p>
     *
     * @param rateLimitRecord
     *            the current {@link GHRateLimit.Record} to check against.
     * @param count
     *            the number of times in a row this method has been called for the current request
     * @return {@code false} if the current request does not exceed the allowed budget, {@code true} if the current
     *         request exceeded the budget.
     * @throws InterruptedException
     *             if the thread is interrupted while sleeping
     */
    protected boolean checkRateLimit(GHRateLimit.Record rateLimitRecord, long count) throws InterruptedException {
        return false;
    }

    protected final boolean sleepUntilReset(GHRateLimit.Record record) throws InterruptedException {
        // Sleep until reset
        long sleepMilliseconds = record.getResetDate().getTime() - System.currentTimeMillis();
        if (sleepMilliseconds > 0) {
            String message = String.format(
                    "GitHub API - Current quota has %d remaining of %d. Waiting for quota to reset at %tT.",
                    record.getRemaining(),
                    record.getLimit(),
                    record.getResetDate());

            LOGGER.log(Level.INFO, message);

            Thread.sleep(sleepMilliseconds);
            return true;
        }
        return false;
    }

    /**
     * A {@link RateLimitChecker} with a simple number as the limit.
     */
    public static class LiteralValue extends RateLimitChecker {
        private final int sleepAtOrBelow;

        public LiteralValue(int sleepAtOrBelow) {
            if (sleepAtOrBelow < 0) {
                throw new IllegalArgumentException("sleepAtOrBelow must >= 0");
            }
            this.sleepAtOrBelow = sleepAtOrBelow;
        }

        @Override
        protected boolean checkRateLimit(GHRateLimit.Record record, long count) throws InterruptedException {
            if (record.getRemaining() <= sleepAtOrBelow) {
                return sleepUntilReset(record);
            }
            return false;
        }

    }

}

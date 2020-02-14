package org.kohsuke.github;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A GitHub API Rate Limit Guard
 * <p>
 * </p>
 */
public abstract class RateLimitChecker {

    private static final Logger LOGGER = Logger.getLogger(RateLimitChecker.class.getName());

    public static final RateLimitChecker NONE = new RateLimitChecker() {
        @Override
        protected boolean checkRateLimit(GHRateLimit.Record record, long count) {
            return false;
        }
    };

    /**
     * Decides what action to take given a rate limit record. Generally this will be to sleep or not.
     *
     * The caller of this method figures out which {@link GHRateLimit.Record} applies for the current request.
     *
     * @param rateLimitRecord
     *            the current {@link GHRateLimit.Record} to check against.
     * @param count
     *            the number of times in a row this wait method has been called.
     */
    protected abstract boolean checkRateLimit(GHRateLimit.Record rateLimitRecord, long count)
            throws InterruptedException;

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

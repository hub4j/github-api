package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static java.util.logging.Level.FINEST;

/**
 * Rate limit.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHRateLimit {

    /**
     * Remaining calls that can be made.
     *
     * @deprecated This value should never have been made public. Use {@link #getRemaining()}
     */
    @Deprecated
    public int remaining;

    /**
     * Allotted API call per hour.
     *
     * @deprecated This value should never have been made public. Use {@link #getLimit()}
     */
    @Deprecated
    public int limit;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds. WARNING: this field was implemented
     * using {@link Date#Date(long)} which expects UTC epoch milliseconds, so this Date instance is meaningless as a
     * date. To use this field in any meaningful way, it must be converted to a long using {@link Date#getTime()}
     * multiplied by 1000.
     *
     * @deprecated This value should never have been made public. Use {@link #getResetDate()}
     */
    @Deprecated
    public Date reset;

    @Nonnull
    private final Record core;

    @Nonnull
    private final Record search;

    @Nonnull
    private final Record graphql;

    @Nonnull
    private final Record integrationManifest;

    @Nonnull
    static GHRateLimit Unknown() {
        return new GHRateLimit(new UnknownLimitRecord(),
                new UnknownLimitRecord(),
                new UnknownLimitRecord(),
                new UnknownLimitRecord());
    }

    @Nonnull
    static GHRateLimit fromHeaderRecord(Record header) {
        return new GHRateLimit(header, new UnknownLimitRecord(), new UnknownLimitRecord(), new UnknownLimitRecord());
    }

    @JsonCreator
    GHRateLimit(@Nonnull @JsonProperty("core") Record core,
            @Nonnull @JsonProperty("search") Record search,
            @Nonnull @JsonProperty("graphql") Record graphql,
            @Nonnull @JsonProperty("integration_manifest") Record integrationManifest) {
        // The Nonnull annotation is ignored by Jackson, we have to check manually
        Objects.requireNonNull(core);
        Objects.requireNonNull(search);
        Objects.requireNonNull(graphql);
        Objects.requireNonNull(integrationManifest);

        this.core = core;
        this.search = search;
        this.graphql = graphql;
        this.integrationManifest = integrationManifest;

        // Deprecated fields
        this.remaining = core.getRemaining();
        this.limit = core.getLimit();
        // This is wrong but is how this was implemented. Kept for backward compat.
        this.reset = new Date(core.getResetEpochSeconds());
    }

    /**
     * Returns the date at which the Core API rate limit will reset.
     *
     * @return the calculated date at which the rate limit has or will reset.
     */
    @Nonnull
    public Date getResetDate() {
        return getCore().getResetDate();
    }

    /**
     * Gets the remaining number of Core APIs requests allowed before this connection will be throttled.
     *
     * @return an integer
     * @since 1.100
     */
    public int getRemaining() {
        return getCore().getRemaining();
    }

    /**
     * Gets the total number of Core API calls per hour allotted for this connection.
     *
     * @return an integer
     * @since 1.100
     */
    public int getLimit() {
        return getCore().getLimit();
    }

    /**
     * Gets the time in epoch seconds when the Core API rate limit will reset.
     *
     * @return a long
     * @since 1.100
     */
    public long getResetEpochSeconds() {
        return getCore().getResetEpochSeconds();
    }

    /**
     * Whether the rate limit reset date for this instance has passed.
     *
     * @return true if the rate limit reset date has passed. Otherwise false.
     * @since 1.100
     */
    public boolean isExpired() {
        return getCore().isExpired();
    }

    /**
     * The core object provides your rate limit status for all non-search-related resources in the REST API.
     *
     * @return a rate limit record
     * @since 1.100
     */
    @Nonnull
    public Record getCore() {
        return core;
    }

    /**
     * The search object provides your rate limit status for the Search API. TODO: integrate with header limit updating.
     * Issue #605.
     *
     * @return a rate limit record
     */
    @Nonnull
    Record getSearch() {
        return search;
    }

    /**
     * The graphql object provides your rate limit status for the GraphQL API. TODO: integrate with header limit
     * updating. Issue #605.
     *
     * @return a rate limit record
     */
    @Nonnull
    Record getGraphQL() {
        return graphql;
    }

    /**
     * The integration_manifest object provides your rate limit status for the GitHub App Manifest code conversion
     * endpoint. TODO: integrate with header limit updating. Issue #605.
     *
     * @return a rate limit record
     */
    @Nonnull
    Record getIntegrationManifest() {
        return integrationManifest;
    }

    @Override
    public String toString() {
        return "GHRateLimit {" + "core " + getCore().toString() + "search " + getSearch().toString() + "graphql "
                + getGraphQL().toString() + "integrationManifest " + getIntegrationManifest().toString() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GHRateLimit rateLimit = (GHRateLimit) o;
        return getCore().equals(rateLimit.getCore()) && getSearch().equals(rateLimit.getSearch())
                && getGraphQL().equals(rateLimit.getGraphQL())
                && getIntegrationManifest().equals(rateLimit.getIntegrationManifest());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCore(), getSearch(), getGraphQL(), getIntegrationManifest());
    }

    /**
     * Gets the appropriate {@link Record} for a particular url path.
     *
     * @param urlPath
     *            the url path of the request
     * @return the {@link Record} for a url path.
     */
    @Nonnull
    Record getRecordForUrlPath(@Nonnull String urlPath) {
        if (urlPath.equals("/rate_limit")) {
            return new UnknownLimitRecord();
        } else if (urlPath.startsWith("/search")) {
            return getSearch();
        } else if (urlPath.startsWith("/graphql")) {
            return getGraphQL();
        } else if (urlPath.startsWith("/app-manifests")) {
            return getIntegrationManifest();
        } else {
            return getCore();
        }
    }

    /**
     * A limit record used as a placeholder when the the actual limit is not known.
     * <p>
     * Has a large limit and long duration so that it will doesn't expire too often.
     *
     * @since 1.100
     */
    public static class UnknownLimitRecord extends Record {

        // One hour
        private static final long unknownLimitResetSeconds = 60L * 60L;

        static final int unknownLimit = 1000000;
        static final int unknownRemaining = 999999;

        private UnknownLimitRecord() {
            super(unknownLimit, unknownRemaining, System.currentTimeMillis() / 1000L + unknownLimitResetSeconds);
        }
    }

    /**
     * A rate limit record.
     *
     * @since 1.100
     */
    public static class Record {
        /**
         * Remaining calls that can be made.
         */
        private final int remaining;

        /**
         * Allotted API call per hour.
         */
        private final int limit;

        /**
         * The time at which the current rate limit window resets in UTC epoch seconds.
         *
         * This is value returned by the server, it is not adjusted if local machine time is not synchronized with
         * server time.
         */
        private final long resetEpochSeconds;

        /**
         * EpochSeconds time (UTC) at which this instance was created.
         */
        private final long createdAtEpochSeconds = System.currentTimeMillis() / 1000;

        /**
         * The calculated time at which the rate limit will reset, adjusted to local machine time even if the local
         * machine time is not synchronized with server time. Recalculated if {@link #recalculateResetDate} is called.
         */
        @Nonnull
        private Date resetDate;

        /**
         * Instantiates a new Record.
         *
         * @param limit
         *            the limit
         * @param remaining
         *            the remaining
         * @param resetEpochSeconds
         *            the reset epoch seconds
         */
        @JsonCreator
        public Record(@JsonProperty(value = "limit", required = true) int limit,
                @JsonProperty(value = "remaining", required = true) int remaining,
                @JsonProperty(value = "reset", required = true) long resetEpochSeconds) {
            this(limit, remaining, resetEpochSeconds, null);
        }

        /**
         * Instantiates a new Record.
         *
         * @param limit
         *            the limit
         * @param remaining
         *            the remaining
         * @param resetEpochSeconds
         *            the reset epoch seconds
         * @param updatedAt
         *            the updated at
         */
        @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Deprecated")
        public Record(int limit, int remaining, long resetEpochSeconds, @CheckForNull String updatedAt) {
            this.limit = limit;
            this.remaining = remaining;
            this.resetEpochSeconds = resetEpochSeconds;
            this.resetDate = recalculateResetDate(updatedAt);
        }

        /**
         * Recalculates the reset date using the server response date to calculate a time duration and then add that to
         * the local created time for this record.
         *
         * @param updatedAt
         *            a string date in RFC 1123
         * @return reset date based on the passed date
         */
        Date recalculateResetDate(@CheckForNull String updatedAt) {
            long updatedAtEpochSeconds = createdAtEpochSeconds;
            if (!StringUtils.isBlank(updatedAt)) {
                try {
                    // Get the server date and reset data, will always return a time in GMT
                    updatedAtEpochSeconds = ZonedDateTime.parse(updatedAt, DateTimeFormatter.RFC_1123_DATE_TIME)
                            .toEpochSecond();
                } catch (DateTimeParseException e) {
                    if (LOGGER.isLoggable(FINEST)) {
                        LOGGER.log(FINEST, "Malformed Date header value " + updatedAt, e);
                    }
                }
            }

            // This may seem odd but it results in an accurate or slightly pessimistic reset date
            // based on system time rather than assuming the system time synchronized with the server
            long calculatedSecondsUntilReset = resetEpochSeconds - updatedAtEpochSeconds;
            return resetDate = new Date((createdAtEpochSeconds + calculatedSecondsUntilReset) * 1000);
        }

        /**
         * Gets the remaining number of requests allowed before this connection will be throttled.
         *
         * @return an integer
         */
        public int getRemaining() {
            return remaining;
        }

        /**
         * Gets the total number of API calls per hour allotted for this connection.
         *
         * @return an integer
         */
        public int getLimit() {
            return limit;
        }

        /**
         * Gets the time in epoch seconds when the rate limit will reset.
         *
         * This is value returned by the server, it is not adjusted if local machine time is not synchronized with
         * server time. If attempting to check when the rate limit will reset, use {@link #getResetDate()} or implement
         * a {@link RateLimitChecker} instead.
         *
         * @return a long representing the time in epoch seconds when the rate limit will reset
         * @see #getResetDate() #getResetDate()
         */
        public long getResetEpochSeconds() {
            return resetEpochSeconds;
        }

        /**
         * Whether the rate limit reset date indicated by this instance is expired
         *
         * @return true if the rate limit reset date has passed. Otherwise false.
         */
        public boolean isExpired() {
            return getResetDate().getTime() < System.currentTimeMillis();
        }

        /**
         * Returns the date at which the rate limit will reset, adjusted to local machine time if it not synchronized
         * with server time.
         *
         * If attempting to check when the rate limit will reset, consider implementing a {@link RateLimitChecker}
         * instead.
         *
         * @return the calculated date at which the rate limit has or will reset.
         */
        @Nonnull
        public Date getResetDate() {
            return new Date(resetDate.getTime());
        }

        @Override
        public String toString() {
            return "{" + "remaining=" + getRemaining() + ", limit=" + getLimit() + ", resetDate=" + getResetDate()
                    + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Record record = (Record) o;
            return getRemaining() == record.getRemaining() && getLimit() == record.getLimit()
                    && getResetEpochSeconds() == record.getResetEpochSeconds()
                    && getResetDate().equals(record.getResetDate());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getRemaining(), getLimit(), getResetEpochSeconds(), getResetDate());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());
}

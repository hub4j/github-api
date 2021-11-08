package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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
     * @deprecated This field should never have been made public. Use {@link #getRemaining()}
     */
    @Deprecated
    public int remaining;

    /**
     * Allotted API call per hour.
     *
     * @deprecated This field should never have been made public. Use {@link #getLimit()}
     */
    @Deprecated
    public int limit;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds. WARNING: this field was implemented
     * using {@link Date#Date(long)} which expects UTC epoch milliseconds, so this Date instance is meaningless as a
     * date. To use this field in any meaningful way, it must be converted to a long using {@link Date#getTime()}
     * multiplied by 1000.
     *
     * @deprecated This field should never have been made public. Use {@link #getResetDate()}
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

    /**
     * The default GHRateLimit provided to new {@link GitHubClient}s.
     *
     * Contains all expired records that will cause {@link GitHubClient#rateLimit(RateLimitTarget)} to refresh with new
     * data when called.
     *
     * Private, but made internal for testing.
     */
    @Nonnull
    static final GHRateLimit DEFAULT = new GHRateLimit(UnknownLimitRecord.DEFAULT,
            UnknownLimitRecord.DEFAULT,
            UnknownLimitRecord.DEFAULT,
            UnknownLimitRecord.DEFAULT);

    /**
     * Creates a new {@link GHRateLimit} from a single record for the specified endpoint with place holders for other
     * records.
     *
     * This is used to create {@link GHRateLimit} instances that can merged with other instances.
     *
     * @param record
     *            the rate limit record. Can be a regular {@link Record} constructed from header information or an
     *            {@link UnknownLimitRecord} placeholder.
     * @param rateLimitTarget
     *            which rate limit record to fill
     * @return a new {@link GHRateLimit} instance containing the supplied record
     */
    @Nonnull
    static GHRateLimit fromRecord(@Nonnull Record record, @Nonnull RateLimitTarget rateLimitTarget) {
        if (rateLimitTarget == RateLimitTarget.CORE || rateLimitTarget == RateLimitTarget.NONE) {
            return new GHRateLimit(record,
                    UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT);
        } else if (rateLimitTarget == RateLimitTarget.SEARCH) {
            return new GHRateLimit(UnknownLimitRecord.DEFAULT,
                    record,
                    UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT);
        } else if (rateLimitTarget == RateLimitTarget.GRAPHQL) {
            return new GHRateLimit(UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT,
                    record,
                    UnknownLimitRecord.DEFAULT);
        } else if (rateLimitTarget == RateLimitTarget.INTEGRATION_MANIFEST) {
            return new GHRateLimit(UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT,
                    UnknownLimitRecord.DEFAULT,
                    record);
        } else {
            throw new IllegalArgumentException("Unknown rate limit target: " + rateLimitTarget.toString());
        }
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
     * Whether the reset date for the Core API rate limit has passed.
     *
     * @return true if the rate limit reset date has passed. Otherwise false.
     * @since 1.100
     */
    public boolean isExpired() {
        return getCore().isExpired();
    }

    /**
     * The core object provides the rate limit status for all non-search-related resources in the REST API.
     *
     * @return a rate limit record
     * @since 1.100
     */
    @Nonnull
    public Record getCore() {
        return core;
    }

    /**
     * The search record provides the rate limit status for the Search API.
     *
     * @return a rate limit record
     * @since 1.115
     */
    @Nonnull
    public Record getSearch() {
        return search;
    }

    /**
     * The graphql record provides the rate limit status for the GraphQL API.
     *
     * @return a rate limit record
     * @since 1.115
     */
    @Nonnull
    public Record getGraphQL() {
        return graphql;
    }

    /**
     * The integration manifest record provides the rate limit status for the GitHub App Manifest code conversion
     * endpoint.
     *
     * @return a rate limit record
     * @since 1.115
     */
    @Nonnull
    public Record getIntegrationManifest() {
        return integrationManifest;
    }

    @Override
    public String toString() {
        return "GHRateLimit {" + "core " + getCore().toString() + ", search " + getSearch().toString() + ", graphql "
                + getGraphQL().toString() + ", integrationManifest " + getIntegrationManifest().toString() + "}";
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
     * Merge a {@link GHRateLimit} with another one to create a new {@link GHRateLimit} keeping the latest
     * {@link Record}s from each.
     *
     * @param newLimit
     *            {@link GHRateLimit} with potentially updated {@link Record}s.
     * @return a merged {@link GHRateLimit} with the latest {@link Record}s from these two instances. If the merged
     *         instance is equal to the current instance, the current instance is returned.
     */
    @Nonnull
    GHRateLimit getMergedRateLimit(@Nonnull GHRateLimit newLimit) {

        GHRateLimit merged = new GHRateLimit(getCore().currentOrUpdated(newLimit.getCore()),
                getSearch().currentOrUpdated(newLimit.getSearch()),
                getGraphQL().currentOrUpdated(newLimit.getGraphQL()),
                getIntegrationManifest().currentOrUpdated(newLimit.getIntegrationManifest()));

        if (merged.equals(this)) {
            merged = this;
        }

        return merged;
    }

    /**
     * Gets the specified {@link Record}.
     *
     * {@link RateLimitTarget#NONE} will return {@link UnknownLimitRecord#DEFAULT} to prevent any clients from
     * accidentally waiting on that record to reset before continuing.
     *
     * @param rateLimitTarget
     *            the target rate limit record
     * @return the target {@link Record} from this instance.
     */
    @Nonnull
    Record getRecord(@Nonnull RateLimitTarget rateLimitTarget) {
        if (rateLimitTarget == RateLimitTarget.CORE) {
            return getCore();
        } else if (rateLimitTarget == RateLimitTarget.SEARCH) {
            return getSearch();
        } else if (rateLimitTarget == RateLimitTarget.GRAPHQL) {
            return getGraphQL();
        } else if (rateLimitTarget == RateLimitTarget.INTEGRATION_MANIFEST) {
            return getIntegrationManifest();
        } else if (rateLimitTarget == RateLimitTarget.NONE) {
            return UnknownLimitRecord.DEFAULT;
        } else {
            throw new IllegalArgumentException("Unknown rate limit target: " + rateLimitTarget.toString());
        }
    }

    /**
     * A limit record used as a placeholder when the the actual limit is not known.
     *
     * @since 1.100
     */
    public static class UnknownLimitRecord extends Record {

        private static final long defaultUnknownLimitResetSeconds = Duration.ofSeconds(30).getSeconds();

        /**
         * The number of seconds until a {@link UnknownLimitRecord} will expire.
         *
         * This is set to a somewhat short duration, rather than a long one. This avoids
         * {@link {@link GitHubClient#rateLimit(RateLimitTarget)}} requesting rate limit updates continuously, but also
         * avoids holding on to stale unknown records indefinitely.
         *
         * When merging {@link GHRateLimit} instances, {@link UnknownLimitRecord}s will be superseded by incoming
         * regular {@link Record}s.
         *
         * @see GHRateLimit#getMergedRateLimit(GHRateLimit)
         */
        static long unknownLimitResetSeconds = defaultUnknownLimitResetSeconds;

        static final int unknownLimit = 1000000;
        static final int unknownRemaining = 999999;

        // The default UnknownLimitRecord is an expired record.
        private static final UnknownLimitRecord DEFAULT = new UnknownLimitRecord(Long.MIN_VALUE);

        // The starting current UnknownLimitRecord is an expired record.
        private static final AtomicReference<UnknownLimitRecord> current = new AtomicReference<>(DEFAULT);

        /**
         * Create a new unknown record that resets at the specified time.
         *
         * @param resetEpochSeconds
         *            the epoch second time when this record will expire.
         */
        private UnknownLimitRecord(long resetEpochSeconds) {
            super(unknownLimit, unknownRemaining, resetEpochSeconds);
        }

        static Record current() {
            Record result = current.get();
            if (result.isExpired()) {
                current.set(new UnknownLimitRecord(System.currentTimeMillis() / 1000L + unknownLimitResetSeconds));
                result = current.get();
            }
            return result;
        }

        /**
         * Reset the current UnknownLimitRecord. For use during testing only.
         */
        static void reset() {
            current.set(DEFAULT);
            unknownLimitResetSeconds = defaultUnknownLimitResetSeconds;
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
         * Allotted API call per time period.
         */
        private final int limit;

        /**
         * The time at which the current rate limit window resets in UTC epoch seconds.
         */
        private final long resetEpochSeconds;

        /**
         * EpochSeconds time (UTC) at which this instance was created.
         */
        private final long createdAtEpochSeconds = System.currentTimeMillis() / 1000;

        /**
         * The date at which the rate limit will reset, adjusted to local machine time if the local machine's clock not
         * synchronized with to the same clock as the GitHub server.
         *
         * @see #calculateResetDate(String)
         * @see #getResetDate()
         */
        @Nonnull
        private final Date resetDate;

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
        public Record(@JsonProperty(value = "limit", required = true) int limit,
                @JsonProperty(value = "remaining", required = true) int remaining,
                @JsonProperty(value = "reset", required = true) long resetEpochSeconds) {
            this(limit, remaining, resetEpochSeconds, null);
        }

        /**
         * Instantiates a new Record. Called by Jackson data binding or during header parsing.
         *
         * @param limit
         *            the limit
         * @param remaining
         *            the remaining
         * @param resetEpochSeconds
         *            the reset epoch seconds
         * @param connectorResponse
         *            the response info
         */
        @JsonCreator
        Record(@JsonProperty(value = "limit", required = true) int limit,
                @JsonProperty(value = "remaining", required = true) int remaining,
                @JsonProperty(value = "reset", required = true) long resetEpochSeconds,
                @JacksonInject @CheckForNull GitHubConnectorResponse connectorResponse) {
            this.limit = limit;
            this.remaining = remaining;
            this.resetEpochSeconds = resetEpochSeconds;
            String updatedAt = null;
            if (connectorResponse != null) {
                updatedAt = connectorResponse.header("Date");
            }
            this.resetDate = calculateResetDate(updatedAt);
        }

        /**
         * Determine if the current {@link Record} is outdated compared to another. Rate Limit dates are only accurate
         * to the second, so we look at other information in the record as well.
         *
         * {@link Record}s with earlier {@link #getResetEpochSeconds()} are replaced by those with later.
         * {@link Record}s with the same {@link #getResetEpochSeconds()} are replaced by those with less remaining
         * count.
         *
         * {@link UnknownLimitRecord}s compare with each other like regular {@link Record}s.
         *
         * {@link Record}s are replaced by {@link UnknownLimitRecord}s only when the current {@link Record} is expired
         * and the {@link UnknownLimitRecord} is not. Otherwise Regular {@link Record}s are not replaced by
         * {@link UnknownLimitRecord}s.
         *
         * Expiration is only considered after other checks, meaning expired records may sometimes be replaced by other
         * expired records.
         *
         * @param other
         *            the other {@link Record}
         * @return the {@link Record} that is most current
         */
        Record currentOrUpdated(@Nonnull Record other) {
            // This set of checks avoids most calls to isExpired()
            // Depends on UnknownLimitRecord.current() to prevent continuous updating of GHRateLimit rateLimit()
            if (getResetEpochSeconds() > other.getResetEpochSeconds()
                    || (getResetEpochSeconds() == other.getResetEpochSeconds()
                            && getRemaining() <= other.getRemaining())) {
                // If the current record has a later reset
                // or the current record has the same reset and fewer or same requests remaining
                // Then it is most recent
                return this;
            } else if (!(other instanceof UnknownLimitRecord)) {
                // If the above is not the case that means other has a later reset
                // or the same resent and fewer requests remaining.
                // If the other record is not an unknown record, the the other is more recent
                return other;
            } else if (this.isExpired() && !other.isExpired()) {
                // The other is an unknown record.
                // If the current record has expired and the other hasn't, return the other.
                return other;
            }

            // If none of the above, the current record is most valid.
            return this;
        }

        /**
         * Recalculates the {@link #resetDate} relative to the local machine clock.
         * <p>
         * {@link RateLimitChecker}s and {@link RateLimitHandler}s use {@link #getResetDate()} to make decisions about
         * how long to wait for until for the rate limit to reset. That means that {@link #getResetDate()} needs to be
         * calculated based on the local machine clock.
         * </p>
         * <p>
         * When we say that the clock on two machines is "synchronized", we mean that the UTC time returned from
         * {@link System#currentTimeMillis()} on each machine is basically the same. For the purposes of rate limits an
         * differences of up to a second can be ignored.
         * </p>
         * <p>
         * When the clock on the local machine is synchronized to the same time as the clock on the GitHub server (via a
         * time service for example), the {@link #resetDate} generated directly from {@link #resetEpochSeconds} will be
         * accurate for the local machine as well.
         * </p>
         * <p>
         * When the clock on the local machine is not synchronized with the server, the {@link #resetDate} must be
         * recalculated relative to the local machine clock. This is done by taking the number of seconds between the
         * response "Date" header and {@link #resetEpochSeconds} and then adding that to this record's
         * {@link #createdAtEpochSeconds}.
         *
         * @param updatedAt
         *            a string date in RFC 1123
         * @return reset date based on the passed date
         */
        @Nonnull
        private Date calculateResetDate(@CheckForNull String updatedAt) {
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
            return new Date((createdAtEpochSeconds + calculatedSecondsUntilReset) * 1000);
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
         * This is the raw value returned by the server. This value is not adjusted if local machine time is not
         * synchronized with server time. If attempting to check when the rate limit will reset, use
         * {@link #getResetDate()} or implement a {@link RateLimitChecker} instead.
         *
         * @return a long representing the time in epoch seconds when the rate limit will reset
         * @see #getResetDate()
         */
        public long getResetEpochSeconds() {
            return resetEpochSeconds;
        }

        /**
         * Whether the rate limit reset date indicated by this instance is expired
         *
         * If attempting to wait for the rate limit to reset, consider implementing a {@link RateLimitChecker} instead.
         *
         * @return true if the rate limit reset date has passed. Otherwise false.
         */
        public boolean isExpired() {
            return getResetDate().getTime() < System.currentTimeMillis();
        }

        /**
         * The date at which the rate limit will reset, adjusted to local machine time if the local machine's clock not
         * synchronized with to the same clock as the GitHub server.
         *
         * If attempting to wait for the rate limit to reset, consider implementing a {@link RateLimitChecker} instead.
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

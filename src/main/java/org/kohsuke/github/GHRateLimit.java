package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

import static java.util.logging.Level.FINEST;

/**
 * Rate limit.
 * @author Kohsuke Kawaguchi
 */
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
     * @deprecated This value should never have been made public.  Use {@link #getLimit()}
     */
    @Deprecated
    public int limit;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds.
     * NOTE: that means to
     *
     * @deprecated This value should never have been made public. Use {@link #getResetDate()}
     */
    @Deprecated
    public Date reset;

    /**
     * Remaining calls that can be made.
     */
    private int remainingCount;

    /**
     * Allotted API call per hour.
     */
    private int limitCount;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds.
     */
    private long resetEpochSeconds = -1;

    /**
     * String representation of the Date header from the response.
     * If null, the value is ignored.
     * Package private and effectively final.
     */
    @CheckForNull
    String updatedAt = null;

    /**
     * EpochSeconds time (UTC) at which this response was updated.
     * Will be updated to match {@link this.updatedAt} if that is not null.
     */
    private long updatedAtEpochSeconds = System.currentTimeMillis() / 1000L;

    /**
     * The calculated time at which the rate limit will reset.
     * Only calculated if {@link #getResetDate} is called.
     */
    @CheckForNull
    private Date calculatedResetDate;

    /**
     * Gets a placeholder instance that can be used when we fail to get one from the server.
     *
     * @return a GHRateLimit
     */
    public static GHRateLimit getPlaceholder() {
        GHRateLimit r = new GHRateLimit();
        r.setLimit(1000000);
        r.setRemaining(1000000);
        long minute = 60L;
        // This make it so that calling rateLimit() multiple times does not result in multiple request
        r.setResetEpochSeconds(System.currentTimeMillis() / 1000L + minute);
        return r;
    }

    /**
     * Gets the remaining number of requests allowed before this connection will be throttled.
     *
     * @return an integer
     */
    @JsonProperty("remaining")
    public int getRemaining() {
        return remainingCount;
    }

    /**
     * Sets the remaining number of requests allowed before this connection will be throttled.
     *
     * @param remaining an integer
     */
    @JsonProperty("remaining")
    void setRemaining(int remaining) {
        this.remainingCount = remaining;
        this.remaining = remaining;
    }


    /**
     * Gets the total number of API calls per hour allotted for this connection.
     *
     * @return an integer
     */
    @JsonProperty("limit")
    public int getLimit() {
        return limitCount;
    }

    /**
     * Sets the total number of API calls per hour allotted for this connection.
     *
     * @param limit an integer
     */
    @JsonProperty("limit")
    void setLimit(int limit) {
        this.limitCount = limit;
        this.limit = limit;
    }

    /**
     * Gets the time in epoch seconds when the rate limit will reset.
     *
     * @return a long
     */
    @JsonProperty("reset")
    public long getResetEpochSeconds() {
        return resetEpochSeconds;
    }

    /**
     * The time in epoch seconds when the rate limit will reset.
     *
     * @param resetEpochSeconds the reset time in epoch seconds
     */
    @JsonProperty("reset")
    void setResetEpochSeconds(long resetEpochSeconds) {
        this.resetEpochSeconds = resetEpochSeconds;
        this.reset = new Date(resetEpochSeconds);
    }

    /**
     * Whether the rate limit reset date indicated by this instance is in the
     *
     * @return true if the rate limit reset date has passed. Otherwise false.
     */
    public boolean isExpired() {
        return getResetDate().getTime() < System.currentTimeMillis();
    }

    /**
     * Calculates the date at which the rate limit will reset.
     * If available, it uses the server time indicated by the Date response header to accurately
     * calculate this date.  If not, it uses the system time UTC.
     *
     * @return the calculated date at which the rate limit has or will reset.
     */
    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "The value comes from JSON deserialization")
    public Date getResetDate() {
        if (calculatedResetDate == null) {
            if (!StringUtils.isBlank(updatedAt)) {
                // this is why we wait to calculate the reset date - it is expensive.
                try {
                    // Get the server date and reset data, will always return a time in GMT
                    updatedAtEpochSeconds = ZonedDateTime.parse(updatedAt, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
                } catch (DateTimeParseException e) {
                    if (LOGGER.isLoggable(FINEST)) {
                        LOGGER.log(FINEST, "Malformed Date header value " + updatedAt, e);
                    }
                }
            }

            long calculatedSecondsUntilReset = resetEpochSeconds - updatedAtEpochSeconds;

            // This may seem odd but it results in an accurate or slightly pessimistic reset date
            calculatedResetDate = new Date((updatedAtEpochSeconds + calculatedSecondsUntilReset) * 1000);
        }

        return calculatedResetDate;
    }

    @Override
    public String toString() {
        return "GHRateLimit{" +
                "remaining=" + getRemaining() +
                ", limit=" + getLimit() +
                ", resetDate=" + getResetDate() +
                '}';
    }

    private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());
}

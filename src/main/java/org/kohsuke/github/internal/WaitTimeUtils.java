package org.kohsuke.github.internal;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;

/**
 * Shared logic for computing how long to wait before retrying a request after GitHub asks the client to slow down.
 *
 * <p>
 * Extracted from {@code GitHubAbuseLimitHandler} and {@code GitHubRateLimitHandler}, which previously each carried
 * their own near-duplicate {@code parseWaitTime} implementation.
 * </p>
 */
public final class WaitTimeUtils {

    /**
     * Computes the wait time described by the {@code X-RateLimit-Reset} header of a response.
     *
     * <p>
     * The header value is a Unix epoch timestamp (in seconds) marking when the rate limit window resets. If the header
     * is missing, {@code defaultWaitMillis} is returned. The result is never smaller than {@code minimumWaitMillis}.
     * </p>
     *
     * @param connectorResponse
     *            the response to read the {@code X-RateLimit-Reset} header from
     * @param defaultWaitMillis
     *            the wait to use when the response carries no {@code X-RateLimit-Reset} header
     * @param minimumWaitMillis
     *            the minimum wait to return, even if the reset time has already passed
     * @return the number of milliseconds to wait before retrying
     */
    public static long parseRateLimitResetWaitTime(@Nonnull GitHubConnectorResponse connectorResponse,
            long defaultWaitMillis,
            long minimumWaitMillis) {
        String v = connectorResponse.header("X-RateLimit-Reset");
        if (v == null) {
            return defaultWaitMillis;
        }

        ZonedDateTime now = serverNow(connectorResponse);
        return Math.max(minimumWaitMillis, (Long.parseLong(v) - now.toInstant().getEpochSecond()) * 1000);
    }

    /**
     * Computes the wait time described by the {@code Retry-After} header of a response.
     *
     * <p>
     * The header value is either a number of seconds or an HTTP-date (the spec allows both). If the header is missing,
     * {@code defaultWaitMillis} is returned. The result is never smaller than {@code minimumWaitMillis}.
     * </p>
     *
     * @param connectorResponse
     *            the response to read the {@code Retry-After} header from
     * @param defaultWaitMillis
     *            the wait to use when the response carries no {@code Retry-After} header
     * @param minimumWaitMillis
     *            the minimum wait to return, even if the response suggests a shorter one
     * @return the number of milliseconds to wait before retrying
     */
    public static long parseRetryAfterWaitTime(@Nonnull GitHubConnectorResponse connectorResponse,
            long defaultWaitMillis,
            long minimumWaitMillis) {
        String v = connectorResponse.header("Retry-After");
        if (v == null) {
            return defaultWaitMillis;
        }

        try {
            return Math.max(minimumWaitMillis, Duration.ofSeconds(Long.parseLong(v)).toMillis());
        } catch (NumberFormatException nfe) {
            // The retry-after header could be a number in seconds, or an http-date
            // We know it was a date if we got a number format exception :)
            ZonedDateTime zdt = ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME);
            return Math.max(minimumWaitMillis, ChronoUnit.MILLIS.between(serverNow(connectorResponse), zdt));
        }
    }

    /**
     * Determines what time the remote server thinks it is, based on the {@code Date} header of the response.
     *
     * <p>
     * The local clock is only used as a fallback, because the local and remote server times may not be in sync.
     * </p>
     */
    private static ZonedDateTime serverNow(GitHubConnectorResponse connectorResponse) {
        // Don't use ZonedDateTime.now(), because the local and remote server times may not be in sync
        // Instead, we can take advantage of the Date field in the response to see what time the remote server
        // thinks it is
        String dateField = connectorResponse.header("Date");
        if (dateField != null) {
            return ZonedDateTime.parse(dateField, DateTimeFormatter.RFC_1123_DATE_TIME);
        } else {
            return ZonedDateTime.now();
        }
    }

    private WaitTimeUtils() {
    }
}

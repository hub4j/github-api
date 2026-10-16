package org.kohsuke.github.internal;

import org.junit.Test;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WaitTimeUtils}, the shared implementation behind {@code GitHubAbuseLimitHandler.parseWaitTime} and
 * {@code GitHubRateLimitHandler.parseWaitTime}.
 */
public class WaitTimeUtilsTest {

    private static final long DEFAULT_WAIT = 61_000L;
    private static final long MINIMUM_WAIT = 1_000L;

    private static String httpDate(ZonedDateTime dateTime) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(dateTime.withZoneSameInstant(ZoneOffset.UTC));
    }

    @Test
    public void rateLimitResetClampsToMinimumWhenResetAlreadyPassed() {
        ZonedDateTime serverNow = ZonedDateTime.of(2026, 10, 16, 12, 0, 0, 0, ZoneOffset.UTC);
        long resetEpochSeconds = serverNow.minusSeconds(90).toInstant().getEpochSecond();

        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Date")).thenReturn(httpDate(serverNow));
        when(response.header("X-RateLimit-Reset")).thenReturn(String.valueOf(resetEpochSeconds));

        assertThat(WaitTimeUtils.parseRateLimitResetWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT),
                equalTo(MINIMUM_WAIT));
    }

    @Test
    public void rateLimitResetComputesWaitFromResponseDate() {
        ZonedDateTime serverNow = ZonedDateTime.of(2026, 10, 16, 12, 0, 0, 0, ZoneOffset.UTC);
        long resetEpochSeconds = serverNow.plusSeconds(90).toInstant().getEpochSecond();

        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Date")).thenReturn(httpDate(serverNow));
        when(response.header("X-RateLimit-Reset")).thenReturn(String.valueOf(resetEpochSeconds));

        assertThat(WaitTimeUtils.parseRateLimitResetWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT), equalTo(90_000L));
    }

    @Test
    public void rateLimitResetReturnsDefaultWhenHeaderMissing() {
        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("X-RateLimit-Reset")).thenReturn(null);

        assertThat(WaitTimeUtils.parseRateLimitResetWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT),
                equalTo(DEFAULT_WAIT));
    }

    @Test
    public void rateLimitResetFallsBackToLocalTimeWhenDateHeaderMissing() {
        // No Date header: serverNow() must fall back to the local clock.
        // The result depends on the wall clock, so assert a range around the expected wait
        // instead of an exact value (the reset timestamp has one-second precision).
        long resetEpochSeconds = ZonedDateTime.now().plusSeconds(120).toInstant().getEpochSecond();

        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Date")).thenReturn(null);
        when(response.header("X-RateLimit-Reset")).thenReturn(String.valueOf(resetEpochSeconds));

        long wait = WaitTimeUtils.parseRateLimitResetWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT);
        assertThat(wait, greaterThanOrEqualTo(115_000L));
        assertThat(wait, lessThanOrEqualTo(120_000L));
    }

    @Test
    public void retryAfterClampsToMinimum() {
        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Retry-After")).thenReturn("0");

        assertThat(WaitTimeUtils.parseRetryAfterWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT), equalTo(MINIMUM_WAIT));
    }

    @Test
    public void retryAfterParsesHttpDateRelativeToResponseDate() {
        ZonedDateTime serverNow = ZonedDateTime.of(2026, 10, 16, 12, 0, 0, 0, ZoneOffset.UTC);

        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Date")).thenReturn(httpDate(serverNow));
        when(response.header("Retry-After")).thenReturn(httpDate(serverNow.plusSeconds(45)));

        assertThat(WaitTimeUtils.parseRetryAfterWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT), equalTo(45_000L));
    }

    @Test
    public void retryAfterParsesSeconds() {
        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Retry-After")).thenReturn("30");

        assertThat(WaitTimeUtils.parseRetryAfterWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT), equalTo(30_000L));
    }

    @Test
    public void retryAfterReturnsDefaultWhenHeaderMissing() {
        GitHubConnectorResponse response = mock(GitHubConnectorResponse.class);
        when(response.header("Retry-After")).thenReturn(null);

        assertThat(WaitTimeUtils.parseRetryAfterWaitTime(response, DEFAULT_WAIT, MINIMUM_WAIT), equalTo(DEFAULT_WAIT));
    }
}

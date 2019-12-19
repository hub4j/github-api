package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for {@link GitHub} static helpers.
 *
 * @author Liam Newman
 */
public class GitHubStaticTest extends Assert {

    @Test
    public void timeRoundTrip() throws Exception {
        Instant instantNow = Instant.now();

        Date instantSeconds = Date.from(instantNow.truncatedTo(ChronoUnit.SECONDS));
        Date instantMillis = Date.from(instantNow.truncatedTo(ChronoUnit.MILLIS));

        // if we happen to land exactly on zero milliseconds, add 1 milli
        if (instantSeconds.equals(instantMillis)) {
            instantMillis = Date.from(instantNow.plusMillis(1).truncatedTo(ChronoUnit.MILLIS));
        }

        // TODO: other formats
        String instantFormatSlash = formatDate(instantMillis, "yyyy/MM/dd HH:mm:ss ZZZZ");
        String instantFormatDash = formatDate(instantMillis, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        String instantFormatMillis = formatDate(instantMillis, "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        String instantSecondsFormatMillis = formatDate(instantSeconds, "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        String instantBadFormat = formatDate(instantMillis, "yy-MM-dd'T'HH:mm'Z'");

        assertThat(GitHub.parseDate(GitHub.printDate(instantSeconds)),
                equalTo(GitHub.parseDate(GitHub.printDate(instantMillis))));

        assertThat(instantSeconds, equalTo(GitHub.parseDate(GitHub.printDate(instantSeconds))));

        // printDate will truncate to the nearest second, so it should not be equal
        assertThat(instantMillis, not(equalTo(GitHub.parseDate(GitHub.printDate(instantMillis)))));

        assertThat(instantSeconds, equalTo(GitHub.parseDate(instantFormatSlash)));

        assertThat(instantSeconds, equalTo(GitHub.parseDate(instantFormatDash)));

        // This parser does not truncate to the nearest second, so it will be equal
        assertThat(instantMillis, equalTo(GitHub.parseDate(instantFormatMillis)));

        assertThat(instantSeconds, equalTo(GitHub.parseDate(instantSecondsFormatMillis)));

        try {
            GitHub.parseDate(instantBadFormat);
            fail("Bad time format should throw.");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Unable to parse the timestamp: " + instantBadFormat));
        }
    }

    @Test
    public void testGitHubRateLimitShouldReplaceRateLimit() throws Exception {

        GHRateLimit.Record unknown0 = GHRateLimit.Unknown().getCore();
        GHRateLimit.Record unknown1 = GHRateLimit.Unknown().getCore();

        GHRateLimit.Record record0 = new GHRateLimit.Record(10, 10, 10L);
        GHRateLimit.Record record1 = new GHRateLimit.Record(10, 9, 10L);
        GHRateLimit.Record record2 = new GHRateLimit.Record(10, 2, 10L);
        GHRateLimit.Record record3 = new GHRateLimit.Record(10, 10, 20L);
        GHRateLimit.Record record4 = new GHRateLimit.Record(10, 5, 20L);

        Thread.sleep(2000);

        GHRateLimit.Record recordWorst = new GHRateLimit.Record(Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE);
        GHRateLimit.Record record00 = new GHRateLimit.Record(10, 10, 10L);
        GHRateLimit.Record unknown2 = GHRateLimit.Unknown().getCore();

        // Rate-limit records maybe created and returned in different orders.
        // We should update to the regular records over unknowns.
        // After that, we should update to the candidate if its limit is lower or its reset is later.

        assertThat("Equivalent unknown should not replace", GitHub.shouldReplace(unknown0, unknown1), is(false));
        assertThat("Equivalent unknown should not replace", GitHub.shouldReplace(unknown1, unknown0), is(false));

        assertThat("Later unknown should replace earlier", GitHub.shouldReplace(unknown2, unknown0), is(true));
        assertThat("Earlier unknown should not replace later", GitHub.shouldReplace(unknown0, unknown2), is(false));

        assertThat("Worst record should replace later unknown", GitHub.shouldReplace(recordWorst, unknown1), is(true));
        assertThat("Unknown should not replace worst record", GitHub.shouldReplace(unknown1, recordWorst), is(false));

        assertThat("Earlier record should replace later worst", GitHub.shouldReplace(record0, recordWorst), is(true));
        assertThat("Later worst record should not replace earlier",
                GitHub.shouldReplace(recordWorst, record0),
                is(false));

        assertThat("Equivalent record should not replace", GitHub.shouldReplace(record0, record00), is(false));
        assertThat("Equivalent record should not replace", GitHub.shouldReplace(record00, record0), is(false));

        assertThat("Lower limit record should replace higher", GitHub.shouldReplace(record1, record0), is(true));
        assertThat("Lower limit record should replace higher", GitHub.shouldReplace(record2, record1), is(true));

        assertThat("Higher limit record should not replace lower", GitHub.shouldReplace(record1, record2), is(false));

        assertThat("Higher limit record with later reset should  replace lower",
                GitHub.shouldReplace(record3, record2),
                is(true));

        assertThat("Lower limit record with later reset should replace higher",
                GitHub.shouldReplace(record4, record1),
                is(true));

        assertThat("Lower limit record with earlier reset should not replace higher",
                GitHub.shouldReplace(record2, record4),
                is(false));

    }

    static String formatDate(Date dt, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(dt);
    }

}

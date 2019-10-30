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

        // TODO: other formats
        String instantFormatSlash = formatDate(instantMillis, "yyyy/MM/dd HH:mm:ss ZZZZ");
        String instantFormatDash = formatDate(instantMillis, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        String instantFormatMillis = formatDate(instantMillis, "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        String instantSecondsFormatMillis = formatDate(instantSeconds, "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        String instantBadFormat = formatDate(instantMillis, "yy-MM-dd'T'HH:mm'Z'");


        assertThat(GitHub.parseDate(GitHub.printDate(instantSeconds)),
            equalTo(GitHub.parseDate(GitHub.printDate(instantMillis))));

        assertThat(instantSeconds,
            equalTo(GitHub.parseDate(GitHub.printDate(instantSeconds))));

        assertThat(instantMillis,
            not(equalTo(GitHub.parseDate(GitHub.printDate(instantMillis)))));

        assertThat(instantSeconds,
            equalTo(GitHub.parseDate(instantFormatSlash)));

        assertThat(instantSeconds,
            equalTo(GitHub.parseDate(instantFormatDash)));

        assertThat(instantMillis,
            equalTo(GitHub.parseDate(instantFormatMillis)));

        assertThat(instantSeconds,
            equalTo(GitHub.parseDate(instantSecondsFormatMillis)));

        try {
            GitHub.parseDate(instantBadFormat);
            fail("Bad time format should throw.");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Unable to parse the timestamp: " + instantBadFormat));
        }
    }

    static String formatDate(Date dt, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(dt);
    }

}

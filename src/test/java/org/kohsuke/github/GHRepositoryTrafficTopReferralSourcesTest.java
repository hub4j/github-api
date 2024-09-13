package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for {@link GHRepositoryTrafficTopReferralSources}.
 */
public class GHRepositoryTrafficTopReferralSourcesTest {

    /**
     * Test the constructor.
     */
    @Test
    public void test() {
        GHRepositoryTrafficTopReferralSources testee = new GHRepositoryTrafficTopReferralSources(1, 2, "referrer");
        assertThat(testee.getCount(), is(equalTo(1)));
        assertThat(testee.getUniques(), is(equalTo(2)));
        assertThat(testee.getReferrer(), is(equalTo("referrer")));
    }
}

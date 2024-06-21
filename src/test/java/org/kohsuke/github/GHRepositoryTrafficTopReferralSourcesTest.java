package org.kohsuke.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class GHRepositoryTrafficTopReferralSourcesTest {
    @Test
    public void test() {
        GHRepositoryTrafficTopReferralSources testee = new GHRepositoryTrafficTopReferralSources(1, 2, "referrer");
        assertThat(testee.getCount(), is(equalTo(1)));
        assertThat(testee.getUniques(), is(equalTo(2)));
        assertThat(testee.getReferrer(), is(equalTo("referrer")));
    }
}

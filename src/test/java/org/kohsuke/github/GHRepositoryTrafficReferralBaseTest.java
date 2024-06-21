package org.kohsuke.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class GHRepositoryTrafficReferralBaseTest {
    @Test
    public void test() {
        GHRepositoryTrafficReferralBase testee = new GHRepositoryTrafficReferralBase(1, 2);
        assertThat(testee.getCount(), is(equalTo(1)));
        assertThat(testee.getUniques(), is(equalTo(2)));
    }
}

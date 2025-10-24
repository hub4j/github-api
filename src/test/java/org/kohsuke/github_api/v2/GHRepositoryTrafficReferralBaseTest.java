package org.kohsuke.github_api.v2;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for {@link GHRepositoryTrafficReferralBase}.
 */
public class GHRepositoryTrafficReferralBaseTest {

    /**
     * Create default GHRepositoryTrafficReferralBaseTest instance
     */
    public GHRepositoryTrafficReferralBaseTest() {
    }

    /**
     * Test the constructor.
     */
    @Test
    public void test() {
        GHRepositoryTrafficReferralBase testee = new GHRepositoryTrafficReferralBase(1, 2);
        assertThat(testee.getCount(), is(equalTo(1)));
        assertThat(testee.getUniques(), is(equalTo(2)));
    }
}

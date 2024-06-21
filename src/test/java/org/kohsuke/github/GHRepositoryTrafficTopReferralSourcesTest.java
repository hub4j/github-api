package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

public class GHRepositoryTrafficTopReferralSourcesTest {
    @Test
    public void test() {
        GHRepositoryTrafficTopReferralSources testee = new GHRepositoryTrafficTopReferralSources(1, 2, "referrer");
        Assert.assertEquals(1, testee.getCount());
        Assert.assertEquals(2, testee.getUniques());
        Assert.assertEquals("referrer", testee.getReferrer());
    }
}

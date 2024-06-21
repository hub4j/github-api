package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

public class GHRepositoryTrafficReferralBaseTest {
    @Test
    public void test() {
        GHRepositoryTrafficReferralBase testee = new GHRepositoryTrafficReferralBase(1, 2);
        Assert.assertEquals(1, testee.getCount());
        Assert.assertEquals(2, testee.getUniques());
    }
}

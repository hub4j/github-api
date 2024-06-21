package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

public class GHRepositoryTrafficTopReferralPathTest {
    @Test
    public void test() {
        GHRepositoryTrafficTopReferralPath testee = new GHRepositoryTrafficTopReferralPath(1, 2, "path", "title");
        Assert.assertEquals(1, testee.getCount());
        Assert.assertEquals(2, testee.getUniques());
        Assert.assertEquals("path", testee.getPath());
        Assert.assertEquals("title", testee.getTitle());
    }
}

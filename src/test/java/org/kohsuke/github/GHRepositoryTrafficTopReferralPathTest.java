package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link GHRepositoryTrafficTopReferralPath}.
 */
public class GHRepositoryTrafficTopReferralPathTest {

    /**
     * Test the constructor.
     */
    @Test
    public void test() {
        GHRepositoryTrafficTopReferralPath testee = new GHRepositoryTrafficTopReferralPath(1, 2, "path", "title");
        assertThat(testee.getCount(), is(equalTo(1)));
        assertThat(testee.getUniques(), is(equalTo(2)));
        assertThat(testee.getPath(), is(equalTo("path")));
        assertThat(testee.getTitle(), is(equalTo("title")));
    }
}

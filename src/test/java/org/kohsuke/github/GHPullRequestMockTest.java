package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GHPullRequestMockTest {

    @Test
    public void shouldMockGHPullRequest() throws IOException {
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        when(pullRequest.isDraft()).thenReturn(true);

        assertTrue("Mock should return true", pullRequest.isDraft());
    }

}

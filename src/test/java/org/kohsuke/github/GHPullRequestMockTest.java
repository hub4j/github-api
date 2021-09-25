package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GHPullRequestMockTest {

    @Test
    public void shouldMockGHPullRequest() throws IOException {
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        when(pullRequest.isDraft()).thenReturn(true);

        assertThat("Mock should return true", pullRequest.isDraft());
    }

}

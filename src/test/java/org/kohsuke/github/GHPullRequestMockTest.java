package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: Auto-generated Javadoc
/**
 * The Class GHPullRequestMockTest.
 */
public class GHPullRequestMockTest {

    /**
     * Create default GHPullRequestMockTest instance
     */
    public GHPullRequestMockTest() {
    }

    /**
     * Should mock GH pull request.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void shouldMockGHPullRequest() throws IOException {
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        when(pullRequest.isDraft()).thenReturn(true);

        assertThat("Mock should return true", pullRequest.isDraft());
    }

    /**
     * Cannot get pull request graphQL id when there is no repository.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void getGraphQLPullRequestIdFailure() throws IOException {
        GHPullRequest pullRequest = new GHPullRequest();

        try {
            pullRequest.getGraphqlPullRequestId();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Repository owner is required to get the pull request ID"));
        }
    }
}

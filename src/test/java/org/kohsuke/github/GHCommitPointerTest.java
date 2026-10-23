package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

// TODO: Auto-generated Javadoc
/**
 * The Class GHCommitPointerTest.
 */
public class GHCommitPointerTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHCommitPointerTest instance
     */
    public GHCommitPointerTest() {
    }

    /**
     * The head of checkstyle/checkstyle#1 comes from a fork that no longer exists, so GitHub reports its repository as
     * {@code null}. The head commit is still reachable through the base repository.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCommitFromDeletedHeadRepository() throws Exception {
        GHPullRequest pullRequest = gitHub.getRepository("checkstyle/checkstyle").getPullRequest(1);
        GHCommitPointer head = pullRequest.getHead();
        assertThat(head.getRepository(), nullValue());
        assertThat(head.getSha(), equalTo("4889a777da54c6848b92da273a28223cfaf8dfe9"));

        GHCommit commit = head.getCommit();
        assertThat(commit, notNullValue());
        assertThat(commit.getSHA1(), equalTo("4889a777da54c6848b92da273a28223cfaf8dfe9"));
        assertThat(commit.getOwner().getFullName(), equalTo("checkstyle/checkstyle"));
    }

    /**
     * A pointer that has neither a repository nor a pull request cannot resolve its commit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCommitWithoutRepositoryOrPullRequest() throws Exception {
        snapshotNotAllowed();
        GHCommitPointer pointer = new GHCommitPointer();
        IOException failure = assertThrows(IOException.class, pointer::getCommit);
        assertThat(failure.getMessage(), equalTo("Repository information is unavailable for commit null"));
    }
}

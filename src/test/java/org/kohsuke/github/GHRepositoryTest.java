package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * @author Liam Newman
 */
public class GHRepositoryTest extends AbstractGitHubApiWireMockTest {

    @Test
    public void archive() throws Exception {
        snapshotNotAllowed();

        // Archive is a one-way action in the API.
        // We do thi this one
        GHRepository repo = getRepository();

        assertThat(repo.isArchived(), is(false));

        repo.archive();

        assertThat(repo.isArchived(), is(true));
        assertThat(getRepository().isArchived(), is(true));
    }

    @Test
    public void getBranch_URLEncoded() throws Exception {
        GHRepository repo = getRepository();
        GHBranch branch = repo.getBranch("test/#UrlEncode");
        assertThat(branch.getName(), is("test/#UrlEncode"));
    }


    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
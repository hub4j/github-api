package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Test class for listing errors in CODEOWNERS files.
 *
 * @author Michael Grant
 */
public class GHCodeownersErrorTest extends AbstractGitHubWireMockTest {

    /**
     * Gets the {@code CODEOWNERS} errors.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testGetCodeownersErrors() throws IOException {
        final GHRepository repo = getRepository(gitHub);
        final List<GHCodeownersError> codeownersErrors = repo.listCodeownersErrors();
        assertThat(codeownersErrors.size(), is(1));
        final GHCodeownersError firstError = codeownersErrors.get(0);
        assertThat(firstError.getLine(), is(1));
        assertThat(firstError.getColumn(), is(3));
        assertThat(firstError.getKind(), is("Unknown owner"));
        assertThat(firstError.getSource(),
                is("* @nonexistent-user # Deliberate error to test response to repo.listCodeownersErrors()\n"));
        assertThat(firstError.getSuggestion(),
                is("make sure @nonexistent-user exists and has write access to the repository"));
        assertThat(firstError.getMessage(),
                is("Unknown owner on line 1: make sure @nonexistent-user exists and has write access to the repository\n\n  * @nonexistent-user # Deliberate error to test response to repo.listCodeownersErrors()\n    ^"));
        assertThat(firstError.getPath(), is(".github/CODEOWNERS"));
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
    }
}

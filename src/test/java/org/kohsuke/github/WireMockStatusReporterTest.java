package org.kohsuke.github;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Tests in this class are meant to show the behavior of {@link AbstractGitHubWireMockTest} with proxying on or off.
 * <p>
 * The wiremock data for these tests should only be modified by hand - thus most are skipped when snapshotting.
 *
 * @author Liam Newman
 */
public class WireMockStatusReporterTest extends AbstractGitHubWireMockTest {

    @Test
    public void user_whenProxying_AuthCorrectlyConfigured() throws Exception {
        snapshotNotAllowed();
        requireProxy("Tests proper configuration when proxying.");

        assertThat(
                "GitHub connection believes it is anonymous.  Make sure you set GITHUB_OAUTH or both GITHUB_USER and GITHUB_PASSWORD environment variables",
                gitHub.isAnonymous(),
                is(false));

        assertThat(gitHub.login, not(equalTo(STUBBED_USER_LOGIN)));

        // If this user query fails, either the proxying config has broken (unlikely)
        // or your auth settings are not being retrieved from the environemnt.
        // Check your settings.
        GHUser user = gitHub.getMyself();
        assertThat(user.getLogin(), notNullValue());

        // System.out.println();
        // System.out.println("WireMockStatusReporterTest: GitHub proxying and user auth correctly configured for user
        // login: " + user.getLogin());
        // System.out.println();
    }

    @Test
    public void user_whenNotProxying_Stubbed() throws Exception {
        snapshotNotAllowed();

        assumeFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

        assertThat(gitHub.isAnonymous(), is(false));
        assertThat(gitHub.login, equalTo(STUBBED_USER_LOGIN));

        GHUser user = gitHub.getMyself();
        // NOTE: the stubbed user does not have to match the login provided from the github object
        // github.login is literally just a placeholder when mocking
        assertThat(user.getLogin(), not(equalTo(STUBBED_USER_LOGIN)));
        assertThat(user.getLogin(), equalTo("stubbed-user-login"));

        // System.out.println("GitHub proxying and user auth correctly configured for user login: " + user.getLogin());
    }

    @Ignore("Can't run this as WireMock will report failure after the test method completes.")
    @Test
    public void BasicBehaviors_whenNotProxying() throws Exception {
        snapshotNotAllowed();
        assumeFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

        Exception e = null;
        GHRepository repo = null;

        // Valid repository, stubbed
        repo = gitHub.getRepository("github-api/github-api");
        assertThat(repo.getDescription(), equalTo("this is a stubbed description"));

        // Invalid repository, without stub - fails 404 when not proxying
        try {
            gitHub.getRepository("jenkinsci/jenkins");
            fail();
        } catch (Exception ex) {
            e = ex;
        }

        assertThat(e, Matchers.<Exception>instanceOf(GHFileNotFoundException.class));
        assertThat(e.getMessage(), containsString("Request was not matched"));

        // Invalid repository, without stub - fails 404 when not proxying
        e = null;
        try {
            gitHub.getRepository("github-api/non-existant-repository");
            fail();
        } catch (Exception ex) {
            e = ex;
        }

        assertThat(e, Matchers.<Exception>instanceOf(GHFileNotFoundException.class));
        assertThat(e.getMessage(), containsString("Request was not matched"));
    }

    @Test
    public void BasicBehaviors_whenProxying() throws Exception {
        snapshotNotAllowed();
        requireProxy("Tests basic behaviors when proxying.");

        Exception e = null;
        GHRepository repo = null;

        // Valid repository, stubbed
        repo = gitHub.getRepository("github-api/github-api");
        assertThat(repo.getDescription(), equalTo("this is a stubbed description"));

        // Valid repository, without stub - succeeds when proxying
        repo = gitHub.getRepository("jenkinsci/jenkins");
        assertThat(repo.getDescription(), notNullValue());

        // Invalid repository, without stub - fails 404 when proxying
        e = null;
        try {
            gitHub.getRepository("github-api/non-existant-repository");
        } catch (Exception ex) {
            e = ex;
        }

        assertThat(e, Matchers.<Exception>instanceOf(GHFileNotFoundException.class));
        assertThat(e.getMessage(),
                equalTo("{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/repos/#get\"}"));
    }

    @Test
    public void whenSnapshot_EnsureProxy() throws Exception {
        assumeTrue("Test only valid when Snapshotting (-Dtest.github.takeSnapshot to enable)",
                mockGitHub.isTakeSnapshot());

        assertTrue("When taking a snapshot, proxy should automatically be enabled", mockGitHub.isUseProxy());
    }

    @Ignore("Not implemented yet")
    @Test
    public void whenSnapshot_EnsureRecordToExpectedLocation() throws Exception {
        assumeTrue("Test only valid when Snapshotting (-Dtest.github.takeSnapshot to enable)",
                mockGitHub.isTakeSnapshot());

    }
}

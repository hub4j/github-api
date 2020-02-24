package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Tests for the GitHub App API methods
 */
public class GHAppTest extends AbstractGitHubWireMockTest {

    protected GitHubBuilder getGitHubBuilder() {
        return super.getGitHubBuilder()
                // ensure that only JWT will be used against the tests below
                .withPassword(null, null)
                .withJwtToken("bogus");
    }

    @Test
    public void getGitHubApp() throws IOException {
        GHApp app = gitHub.getApp();
        assertThat(app.id, is((long) 11111));
        assertThat(app.getOwner().id, is((long) 111111111));
        assertThat(app.getOwner().login, is("bogus"));
        assertThat(app.getName(), is("Bogus-Development"));
        assertThat(app.getDescription(), is(""));
        assertThat(app.getExternalUrl(), is("https://bogus.domain.com"));
        assertThat(app.getHtmlUrl().toString(), is("https://github.com/apps/bogus-development"));
        assertThat(app.getCreatedAt(), is(GitHubClient.parseDate("2019-06-10T04:21:41Z")));
        assertThat(app.getUpdatedAt(), is(GitHubClient.parseDate("2019-06-10T04:21:41Z")));
        assertThat(app.getPermissions().size(), is(4));
        assertThat(app.getEvents().size(), is(2));
        assertThat(app.getInstallationsCount(), is((long) 1));
    }

    @Test
    public void listInstallations() throws IOException {
        GHApp app = gitHub.getApp();
        List<GHAppInstallation> installations = app.listInstallations().toList();
        assertThat(installations.size(), is(1));

        GHAppInstallation appInstallation = installations.get(0);
        testAppInstallation(appInstallation);
    }

    @Test
    public void getInstallationById() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationById(1111111);
        testAppInstallation(installation);
    }

    @Test
    public void getInstallationByOrganization() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByOrganization("bogus");
        testAppInstallation(installation);
    }

    @Test
    public void getInstallationByRepository() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByRepository("bogus", "bogus");
        testAppInstallation(installation);
    }

    @Test
    public void getInstallationByUser() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        testAppInstallation(installation);
    }

    @Test
    public void deleteInstallation() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        try {
            installation.deleteInstallation();
        } catch (IOException e) {
            fail("deleteInstallation wasn't suppose to fail in this test");
        }
    }

    @Test
    public void createToken() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");

        Map<String, GHPermissionType> permissions = new HashMap<String, GHPermissionType>();
        permissions.put("checks", GHPermissionType.WRITE);
        permissions.put("pull_requests", GHPermissionType.WRITE);
        permissions.put("contents", GHPermissionType.READ);
        permissions.put("metadata", GHPermissionType.READ);

        // Create token specifying both permissions and repository ids
        GHAppInstallationToken installationToken = installation.createToken(permissions)
                .repositoryIds(Collections.singletonList((long) 111111111))
                .create();

        assertThat(installationToken.getToken(), is("bogus"));
        assertThat(installation.getPermissions(), is(permissions));
        assertThat(installationToken.getRepositorySelection(), is(GHRepositorySelection.SELECTED));
        assertThat(installationToken.getExpiresAt(), is(GitHubClient.parseDate("2019-08-10T05:54:58Z")));

        GHRepository repository = installationToken.getRepositories().get(0);
        assertThat(installationToken.getRepositories().size(), is(1));
        assertThat(repository.getId(), is((long) 111111111));
        assertThat(repository.getName(), is("bogus"));

        // Create token with no payload
        GHAppInstallationToken installationToken2 = installation.createToken().create();

        assertThat(installationToken2.getToken(), is("bogus"));
        assertThat(installationToken2.getPermissions().size(), is(4));
        assertThat(installationToken2.getRepositorySelection(), is(GHRepositorySelection.ALL));
        assertThat(installationToken2.getExpiresAt(), is(GitHubClient.parseDate("2019-12-19T12:27:59Z")));

        assertNull(installationToken2.getRepositories());;
    }

    private void testAppInstallation(GHAppInstallation appInstallation) throws IOException {
        Map<String, GHPermissionType> appPermissions = appInstallation.getPermissions();
        GHUser appAccount = appInstallation.getAccount();

        assertThat(appInstallation.id, is((long) 11111111));
        assertThat(appAccount.id, is((long) 111111111));
        assertThat(appAccount.login, is("bogus"));
        assertThat(appInstallation.getRepositorySelection(), is(GHRepositorySelection.SELECTED));
        assertThat(appInstallation.getAccessTokenUrl(), endsWith("/app/installations/11111111/access_tokens"));
        assertThat(appInstallation.getRepositoriesUrl(), endsWith("/installation/repositories"));
        assertThat(appInstallation.getAppId(), is((long) 11111));
        assertThat(appInstallation.getTargetId(), is((long) 111111111));
        assertThat(appInstallation.getTargetType(), is(GHTargetType.ORGANIZATION));

        Map<String, GHPermissionType> permissionsMap = new HashMap<String, GHPermissionType>();
        permissionsMap.put("checks", GHPermissionType.WRITE);
        permissionsMap.put("pull_requests", GHPermissionType.WRITE);
        permissionsMap.put("contents", GHPermissionType.READ);
        permissionsMap.put("metadata", GHPermissionType.READ);
        assertThat(appPermissions, is(permissionsMap));

        List<GHEvent> events = Arrays.asList(GHEvent.PULL_REQUEST, GHEvent.PUSH);
        assertThat(appInstallation.getEvents(), containsInAnyOrder(events.toArray(new GHEvent[0])));
        assertThat(appInstallation.getCreatedAt(), is(GitHubClient.parseDate("2019-07-04T01:19:36.000Z")));
        assertThat(appInstallation.getUpdatedAt(), is(GitHubClient.parseDate("2019-07-30T22:48:09.000Z")));
        assertNull(appInstallation.getSingleFileName());
    }

}

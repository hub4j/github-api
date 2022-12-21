package org.kohsuke.github;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

/**
 * <p>
 * Note : As the code scanning alerts cannot be tailored as part of test setup, lot of the test cases are dependent on
 * manual setup of the mock repo. Assertions and verifications will often simply check that the values are non-null
 * rather than depending on hard-coded values, to prevent making the tests flimsy
 * </p>
 */
public class GHCodeScanningAlertInstanceTest extends AbstractGitHubWireMockTest {
    private static final String REPO_NAME = "Pixi";
    private GHCodeScanningAlert alert;

    @Before
    public void setUp() throws Exception {
        GHRepository repo = gitHub.getRepository(GITHUB_API_TEST_ORG + "/" + REPO_NAME);
        alert = getAlertFromRepo(repo);
    }

    private GHCodeScanningAlert getAlertFromRepo(GHRepository repo) {
        List<GHCodeScanningAlert> dismissedAlerts = repo.listCodeScanningAlerts(GHCodeScanningAlertState.DISMISSED)
                ._iterator(1)
                .nextPage();
        Assume.assumeThat(dismissedAlerts.size(), greaterThanOrEqualTo(1));
        return dismissedAlerts.get(0);
    }

    @Test
    public void testListAlertInstances() throws IOException {
        // Arrange

        // Act
        List<GHCodeScanningAlertInstance> results = alert.listAlertInstances().toList();

        // Assert
        assertThat(results.size(), greaterThanOrEqualTo(1));
        GHCodeScanningAlertInstance instance = results.get(0);
        // Can't assert on exact values with having to hardcode values from
        // json file, hence making the assertions generics
        assertThat(instance.getRef(), not((Object) null));
        assertThat(instance.getCommitSha(), not((Object) null));
        assertThat(instance.getState(), not((Object) null));
        assertThat(instance.getMessage(), not((Object) null));
        assertThat(instance.getLocation(), not((Object) null));

        GHCodeScanningAlertInstance.Location location = instance.getLocation();
        // Can't assert on exact values with having to hardcode values from
        // json file, hence making the assertions generics
        assertThat(location.getPath(), not((Object) null));
        assertThat(location.getStartLine(), greaterThanOrEqualTo(0L));
        assertThat(location.getEndLine(), greaterThanOrEqualTo(0L));
        assertThat(location.getStartColumn(), greaterThanOrEqualTo(0L));
        assertThat(location.getStartColumn(), greaterThanOrEqualTo(0L));
    }
}

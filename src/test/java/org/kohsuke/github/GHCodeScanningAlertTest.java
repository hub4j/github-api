package org.kohsuke.github;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.*;

/**
 * <p>
 * Note : As the code scanning alerts cannot be tailored as part of test setup, lot of the test cases are dependent on
 * manual setup of the mock repo. Assertions and verifications will often simply check that the values are non-null
 * rather than depending on hard-coded values, to prevent making the tests flimsy
 * </p>
 */
public class GHCodeScanningAlertTest extends AbstractGitHubWireMockTest {
    private static final String REPO_NAME = "Pixi";
    private GHRepository repo;

    /**
     * Set up the test with alerts from a purpose-made repo
     *
     * @throws Exception
     *             trouble
     */
    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(GITHUB_API_TEST_ORG + "/" + REPO_NAME);
    }

    /**
     * Check that we can get a list of alerts for a repo and that the response contains values in its required fields.
     */
    @Test
    public void testListCodeScanningAlerts() {
        // Arrange

        // Act - Search by filtering on code scanning tool
        List<GHCodeScanningAlert> codeQlAlerts = repo.listCodeScanningAlerts("CodeQL")._iterator(2).nextPage();

        // Assert
        assertThat(codeQlAlerts.size(), equalTo(2)); // This assertion is based on manual setup done on repo to
                                                     // guarantee there are atleast 2 issues

        GHCodeScanningAlert alert = codeQlAlerts.get(0);

        // Verify the code scanning tool details
        assertThat(alert.getTool(), not((Object) null));
        GHCodeScanningAlert.Tool tool = alert.getTool();
        assertThat(tool.getName(), is("CodeQL"));
        assertThat(tool.getVersion(), isA(String.class));
        assertThat(tool.getGuid(), anyOf(nullValue(), isA(String.class)));

        // Verify that fields of the code scanning rule are non-null
        assertThat(alert.getRule(), not((Object) null));
        GHCodeScanningAlert.Rule rule = alert.getRule();
        assertThat(rule.getId(), not((Object) null));
        assertThat(rule.getName(), not((Object) null));
        assertThat(rule.getSeverity(), not((Object) null));
        assertThat(rule.getDescription(), not((Object) null));
        assertThat(rule.getSecuritySeverityLevel(), anyOf(nullValue(), instanceOf(String.class)));

        // Act - Search by filtering on alert status
        List<GHCodeScanningAlert> openAlerts = repo.listCodeScanningAlerts(GHCodeScanningAlertState.OPEN)
                ._iterator(2)
                .nextPage(); // This assertion is based on manual setup done on repo to
        // guarantee there are atleast 2 issues

        // Assert
        assertThat(openAlerts.size(), equalTo(2));
        GHCodeScanningAlert openAlert = openAlerts.get(0);
        assertThat(openAlert.getState(), is(GHCodeScanningAlertState.OPEN));
    }

    /**
     * Get the data for a single alert and verify that the additional details are filled in.
     *
     * @throws IOException
     *             encountered an error while retrieving a response
     * @throws InvocationTargetException
     *             tried to reflectively invoke a method incorrectly
     * @throws IllegalAccessException
     *             tried to reflectively invoke a method that didn't want to be called
     */
    @Test
    public void testGetCodeScanningAlert() throws IOException, InvocationTargetException, IllegalAccessException {
        // Arrange
        List<GHCodeScanningAlert> dismissedAlerts = repo.listCodeScanningAlerts(GHCodeScanningAlertState.DISMISSED)
                ._iterator(1)
                .nextPage();
        Assume.assumeThat(dismissedAlerts.size(), greaterThanOrEqualTo(1));
        GHCodeScanningAlert dismissedAlert = dismissedAlerts.get(0);
        long idOfDismissed = dismissedAlert.getId();

        // Act
        GHCodeScanningAlert result = repo.getCodeScanningAlert(idOfDismissed);

        // Assert
        assertThat(result, not((Object) null));
        assertThat(result.getId(), equalTo(idOfDismissed));
        assertThat(result.getDismissedReason(), equalTo(dismissedAlert.getDismissedReason()));
        assertThat(result.getDismissedAt(), equalTo(dismissedAlert.getDismissedAt()));
        assertThat(result.getDismissedBy().login, equalTo(dismissedAlert.getDismissedBy().login));
        assertThat(result.getHtmlUrl(), equalTo(dismissedAlert.getHtmlUrl()));
        assertThat(result.getMostRecentInstance(), equalToObject(dismissedAlert.getMostRecentInstance()));

        GHCodeScanningAlert.Rule rule = result.getRule();
        assertThat(rule.getId(), not((Object) null));
        assertThat(rule.getSeverity(), not((Object) null));
        assertThat(rule.getDescription(), not((Object) null));
        assertThat(rule.getName(), not((Object) null));

        // The following fields are exclusive to getCodeScanningAlert's response
        assertThat(rule.getFullDescription(), not((Object) null));
        assertThat(rule.getTags(), arrayWithSize(greaterThan(0)));
        assertThat(rule.getHelp(), not((Object) null));
        assertThat(rule.getHelpUri(), anyOf(nullValue(), isA(String.class)));

        // A little redundant, but we should enforce that Nonnull getters return a value
        for (Method m : GHCodeScanningAlert.Rule.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Nonnull.class)) {
                assertThat(m.invoke(rule), notNullValue());
            }
        }
    }

}

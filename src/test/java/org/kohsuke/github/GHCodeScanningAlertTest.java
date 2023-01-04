// package org.kohsuke.github;
//
// import org.junit.Assume;
// import org.junit.Before;
// import org.junit.Test;
//
// import java.io.IOException;
// import java.util.List;
//
// import static org.hamcrest.Matchers.equalTo;
// import static org.hamcrest.Matchers.greaterThanOrEqualTo;
// import static org.hamcrest.Matchers.is;
// import static org.hamcrest.Matchers.not;
//
/// **
// * <p>
// * Note : As the code scanning alerts cannot be tailored as part of test setup, lot of the test cases are dependent on
// * manual setup of the mock repo. Assertions and verifications will often simply check that the values are non-null
// * rather than depending on hard-coded values, to prevent making the tests flimsy
// * </p>
// */
// public class GHCodeScanningAlertTest extends AbstractGitHubWireMockTest {
// private static final String REPO_NAME = "Pixi";
// private GHRepository repo;
//
// /**
// * Gets the mock repo
// *
// * @throws Exception
// * the exception
// */
// @Before
// public void setUp() throws Exception {
// repo = gitHub.getRepository(GITHUB_API_TEST_ORG + "/" + REPO_NAME);
// }
//
// /**
// * Test list code scanning alert payload
// */
// @Test
// public void testListCodeScanningAlerts() {
// // Arrange
//
// // Act - Search by filtering on code scanning tool
// List<GHCodeScanningAlert> codeQlAlerts = repo.listCodeScanningAlerts("CodeQL")._iterator(2).nextPage();
//
// // Assert
// assertThat(codeQlAlerts.size(), equalTo(2)); // This assertion is based on manual setup done on repo to
// // guarantee there are atleast 2 issues
//
// GHCodeScanningAlert alert = codeQlAlerts.get(0);
//
// // Verify the code scanning tool details
// assertThat(alert.getTool(), not((Object) null));
// GHCodeScanningAlert.Tool tool = alert.getTool();
// assertThat(tool.getName(), is("CodeQL"));
// assertThat(tool.getVersion(), not((Object) null));
//
// // Verify that fields of the code scanning rule are non-null
// assertThat(alert.getRule(), not((Object) null));
// GHCodeScanningAlert.Rule rule = alert.getRule();
// assertThat(rule.getId(), not((Object) null));
// assertThat(rule.getName(), not((Object) null));
// assertThat(rule.getSeverity(), not((Object) null));
//
// // Act - Search by filtering on alert status
// List<GHCodeScanningAlert> openAlerts = repo.listCodeScanningAlerts(GHCodeScanningAlertState.OPEN)
// ._iterator(2)
// .nextPage(); // This assertion is based on manual setup done on repo to
// // guarantee there are atleast 2 issues
//
// // Assert
// assertThat(openAlerts.size(), equalTo(2));
// GHCodeScanningAlert openAlert = openAlerts.get(0);
// assertThat(openAlert.getState(), is(GHCodeScanningAlertState.OPEN));
// }
//
// /**
// * Test get code scanning alert payload
// *
// * @throws IOException
// * Signals that an I/O exception has occurred.
// */
// @Test
// public void testGetCodeScanningAlert() throws IOException {
// // Arrange
// List<GHCodeScanningAlert> dismissedAlerts = repo.listCodeScanningAlerts(GHCodeScanningAlertState.DISMISSED)
// ._iterator(1)
// .nextPage();
// Assume.assumeThat(dismissedAlerts.size(), greaterThanOrEqualTo(1));
// GHCodeScanningAlert dismissedAlert = dismissedAlerts.get(0);
// long idOfDismissed = dismissedAlert.getId();
//
// // Act
// GHCodeScanningAlert result = repo.getCodeScanningAlert(idOfDismissed);
//
// // Assert
// assertThat(result, not((Object) null));
// assertThat(result.getId(), equalTo(idOfDismissed));
// assertThat(result.getDismissedReason(), equalTo(dismissedAlert.getDismissedReason()));
// assertThat(result.getDismissedAt(), equalTo(dismissedAlert.getDismissedAt()));
// assertThat(result.getDismissedBy().login, equalTo(dismissedAlert.getDismissedBy().login));
// }
//
// }

package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc

/**
 * The Class GHAppInstallationTest.
 */
public class GHAppInstallationTest extends AbstractGHAppInstallationTest {

    /**
     * Create default GHAppInstallationTest instance
     */
    public GHAppInstallationTest() {
    }

    /**
     * Test list repositories no permissions.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetMarketplaceAccount() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider3.getEncodedAuthorization());

        GHMarketplaceAccountPlan marketplaceAccount = appInstallation.getMarketplaceAccount();
        GHMarketplacePlanTest.testMarketplaceAccount(marketplaceAccount);

        GHMarketplaceAccountPlan plan = marketplaceAccount.getPlan();
        assertThat(plan.getType(), equalTo(GHMarketplaceAccountType.ORGANIZATION));
    }

    /**
     * Test list repositories no permissions.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListRepositoriesNoPermissions() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider2.getEncodedAuthorization());

        assertThat("App does not have permissions and should have 0 repositories",
                appInstallation.listRepositories().toList().isEmpty());
    }

    /**
     * Test list repositories two repos.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider1.getEncodedAuthorization());

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertThat(repositories.size(), equalTo(2));
        assertThat(repositories.stream().map(GHRepository::getName).toArray(),
                arrayContainingInAnyOrder("empty", "test-readme"));
    }

    /**
     * Test list installations, and one of the installations has been suspended.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListSuspendedInstallation() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider1.getEncodedAuthorization());

        final GHUser suspendedBy = appInstallation.getSuspendedBy();
        assertThat(suspendedBy.getLogin(), equalTo("gilday"));

        final Instant suspendedAt = appInstallation.getSuspendedAt();
        final Instant expectedSuspendedAt = LocalDateTime.of(2024, Month.FEBRUARY, 26, 2, 43, 12)
                .toInstant(ZoneOffset.UTC);
        assertThat(suspendedAt, equalTo(expectedSuspendedAt));
    }

}

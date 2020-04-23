package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHBranchProtection.EnforceAdmins;
import org.kohsuke.github.GHBranchProtection.RequiredReviews;
import org.kohsuke.github.GHBranchProtection.RequiredStatusChecks;

public class GHBranchProtectionTest extends AbstractGitHubWireMockTest {
    private static final String BRANCH = "master";
    private static final String BRANCH_REF = "heads/" + BRANCH;

    private GHBranch branch;

    private GHRepository repo;

    @Before
    public void setUp() throws Exception {
        repo = getTempRepository();
        branch = repo.getBranch(BRANCH);
    }

    @Test
    public void testEnableBranchProtections() throws Exception {
        // team/user restrictions require an organization repo to test against
        GHBranchProtection protection = branch.enableProtection()
                .addRequiredChecks("test-status-check")
                .requireBranchIsUpToDate()
                .requireCodeOwnReviews()
                .dismissStaleReviews()
                .requiredReviewers(2)
                .includeAdmins()
                .enable();

        RequiredStatusChecks statusChecks = protection.getRequiredStatusChecks();
        assertNotNull(statusChecks);
        assertTrue(statusChecks.isRequiresBranchUpToDate());
        assertTrue(statusChecks.getContexts().contains("test-status-check"));

        RequiredReviews requiredReviews = protection.getRequiredReviews();
        assertNotNull(requiredReviews);
        assertTrue(requiredReviews.isDismissStaleReviews());
        assertTrue(requiredReviews.isRequireCodeOwnerReviews());
        assertEquals(2, requiredReviews.getRequiredReviewers());

        EnforceAdmins enforceAdmins = protection.getEnforceAdmins();
        assertNotNull(enforceAdmins);
        assertTrue(enforceAdmins.isEnabled());
    }

    @Test
    public void testEnableProtectionOnly() throws Exception {
        branch.enableProtection().enable();
        assertTrue(repo.getBranch(BRANCH).isProtected());
    }

    @Test
    public void testDisableProtectionOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();
        assertTrue(repo.getBranch(BRANCH).isProtected());
        branch.disableProtection();
        assertFalse(repo.getBranch(BRANCH).isProtected());
    }

    @Test
    public void testEnableRequireReviewsOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection().requireReviews().enable();

        assertNotNull(protection.getRequiredReviews());
    }

    @Test
    public void testSignedCommits() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();

        assertFalse(protection.getRequiredSignatures());

        protection.enabledSignedCommits();
        assertTrue(protection.getRequiredSignatures());

        protection.disableSignedCommits();
        assertFalse(protection.getRequiredSignatures());
    }

    @Test
    public void testGetProtection() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();
        GHBranchProtection protectionTest = repo.getBranch(BRANCH).getProtection();
        assertTrue(protectionTest instanceof GHBranchProtection);
        assertTrue(repo.getBranch(BRANCH).isProtected());
    }
}

package org.kohsuke.github;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHBranchProtection.EnforceAdmins;
import org.kohsuke.github.GHBranchProtection.RequiredReviews;
import org.kohsuke.github.GHBranchProtection.RequiredStatusChecks;

import java.io.FileNotFoundException;

@Ignore("ignored as out of scope of SonarSource's fork's changes")
public class GHBranchProtectionTest extends AbstractGitHubApiTestBase {
    private static final String BRANCH = "bp-test";
    private static final String BRANCH_REF = "heads/" + BRANCH;

    private GHBranch branch;

    private GHRepository repo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        repo = gitHub.getRepository("github-api-test-org/GHContentIntegrationTest").fork();

        try {
            repo.getRef(BRANCH_REF);
        } catch (FileNotFoundException e) {
            repo.createRef("refs/" + BRANCH_REF, repo.getBranch("master").getSHA1());
        }

        branch = repo.getBranch(BRANCH);

        if (branch.isProtected()) {
            branch.disableProtection();
        }

        branch = repo.getBranch(BRANCH);
        assertFalse(branch.isProtected());
    }

    @Test
    public void testEnableBranchProtections() throws Exception {
        // team/user restrictions require an organization repo to test against
        GHBranchProtection protection = branch.enableProtection()
                .addRequiredChecks("test-status-check")
                .requireBranchIsUpToDate()
                .requireCodeOwnReviews()
                .dismissStaleReviews()
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
    public void testEnableRequireReviewsOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection()
                .requireReviews()
                .enable();
        
       assertNotNull(protection.getRequiredReviews());
    }
}

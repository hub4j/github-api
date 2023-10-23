package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHBranchProtection.AllowDeletions;
import org.kohsuke.github.GHBranchProtection.AllowForcePushes;
import org.kohsuke.github.GHBranchProtection.AllowForkSyncing;
import org.kohsuke.github.GHBranchProtection.BlockCreations;
import org.kohsuke.github.GHBranchProtection.EnforceAdmins;
import org.kohsuke.github.GHBranchProtection.LockBranch;
import org.kohsuke.github.GHBranchProtection.RequiredConversationResolution;
import org.kohsuke.github.GHBranchProtection.RequiredLinearHistory;
import org.kohsuke.github.GHBranchProtection.RequiredReviews;
import org.kohsuke.github.GHBranchProtection.RequiredStatusChecks;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHBranchProtectionTest.
 */
public class GHBranchProtectionTest extends AbstractGitHubWireMockTest {
    private static final String BRANCH = "main";
    private static final String BRANCH_REF = "heads/" + BRANCH;

    private GHBranch branch;

    private GHRepository repo;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        repo = getTempRepository();
        branch = repo.getBranch(BRANCH);
    }

    /**
     * Test enable branch protections.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testEnableBranchProtections() throws Exception {
        // team/user restrictions require an organization repo to test against
        GHBranchProtection protection = branch.enableProtection()
                .addRequiredChecks("test-status-check")
                .requireBranchIsUpToDate()
                .requireCodeOwnReviews()
                .requireLastPushApproval()
                .dismissStaleReviews()
                .requiredReviewers(2)
                .allowDeletions()
                .allowForcePushes()
                .allowForkSyncing()
                .blockCreations()
                .includeAdmins()
                .lockBranch()
                .requiredConversationResolution()
                .requiredLinearHistory()
                .enable();

        verifyBranchProtection(protection);

        // Get goes through a different code path. Make sure it also gets the correct data.
        protection = branch.getProtection();
        verifyBranchProtection(protection);
    }

    private void verifyBranchProtection(GHBranchProtection protection) {
        RequiredStatusChecks statusChecks = protection.getRequiredStatusChecks();
        assertThat(statusChecks, notNullValue());
        assertThat(statusChecks.isRequiresBranchUpToDate(), is(true));
        assertThat(statusChecks.getContexts(), contains("test-status-check"));

        RequiredReviews requiredReviews = protection.getRequiredReviews();
        assertThat(requiredReviews, notNullValue());
        assertThat(requiredReviews.isDismissStaleReviews(), is(true));
        assertThat(requiredReviews.isRequireCodeOwnerReviews(), is(true));
        assertThat(requiredReviews.isRequireLastPushApproval(), is(true));
        assertThat(requiredReviews.getRequiredReviewers(), equalTo(2));

        AllowDeletions allowDeletions = protection.getAllowDeletions();
        assertThat(allowDeletions, notNullValue());
        assertThat(allowDeletions.isEnabled(), is(true));

        AllowForcePushes allowForcePushes = protection.getAllowForcePushes();
        assertThat(allowForcePushes, notNullValue());
        assertThat(allowForcePushes.isEnabled(), is(true));

        AllowForkSyncing allowForkSyncing = protection.getAllowForkSyncing();
        assertThat(allowForkSyncing, notNullValue());
        assertThat(allowForkSyncing.isEnabled(), is(true));

        BlockCreations blockCreations = protection.getBlockCreations();
        assertThat(blockCreations, notNullValue());
        assertThat(blockCreations.isEnabled(), is(true));

        EnforceAdmins enforceAdmins = protection.getEnforceAdmins();
        assertThat(enforceAdmins, notNullValue());
        assertThat(enforceAdmins.isEnabled(), is(true));

        LockBranch lockBranch = protection.getLockBranch();
        assertThat(lockBranch, notNullValue());
        assertThat(lockBranch.isEnabled(), is(true));

        RequiredConversationResolution requiredConversationResolution = protection.getRequiredConversationResolution();
        assertThat(requiredConversationResolution, notNullValue());
        assertThat(requiredConversationResolution.isEnabled(), is(true));

        RequiredLinearHistory requiredLinearHistory = protection.getRequiredLinearHistory();
        assertThat(requiredLinearHistory, notNullValue());
        assertThat(requiredLinearHistory.isEnabled(), is(true));
    }

    /**
     * Test enable protection only.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testEnableProtectionOnly() throws Exception {
        branch.enableProtection().enable();
        assertThat(repo.getBranch(BRANCH).isProtected(), is(true));
    }

    /**
     * Test disable protection only.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDisableProtectionOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();
        assertThat(repo.getBranch(BRANCH).isProtected(), is(true));
        branch.disableProtection();
        assertThat(repo.getBranch(BRANCH).isProtected(), is(false));
    }

    /**
     * Test enable require reviews only.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testEnableRequireReviewsOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection().requireReviews().enable();

        RequiredReviews requiredReviews = protection.getRequiredReviews();
        assertThat(protection.getRequiredReviews(), notNullValue());
        assertThat(requiredReviews.isDismissStaleReviews(), is(false));
        assertThat(requiredReviews.isRequireCodeOwnerReviews(), is(false));
        assertThat(protection.getRequiredReviews().getRequiredReviewers(), equalTo(1));

        // Get goes through a different code path. Make sure it also gets the correct data.
        protection = branch.getProtection();
        requiredReviews = protection.getRequiredReviews();

        assertThat(protection.getRequiredReviews(), notNullValue());
        assertThat(requiredReviews.isDismissStaleReviews(), is(false));
        assertThat(requiredReviews.isRequireCodeOwnerReviews(), is(false));
        assertThat(protection.getRequiredReviews().getRequiredReviewers(), equalTo(1));
    }

    /**
     * Test signed commits.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testSignedCommits() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();

        assertThat(protection.getRequiredSignatures(), is(false));

        protection.enabledSignedCommits();
        assertThat(protection.getRequiredSignatures(), is(true));

        protection.disableSignedCommits();
        assertThat(protection.getRequiredSignatures(), is(false));
    }

    /**
     * Test get protection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetProtection() throws Exception {
        GHBranchProtection protection = branch.enableProtection().enable();
        GHBranchProtection protectionTest = repo.getBranch(BRANCH).getProtection();
        Boolean condition = protectionTest instanceof GHBranchProtection;
        assertThat(protectionTest, instanceOf(GHBranchProtection.class));
        assertThat(repo.getBranch(BRANCH).isProtected(), is(true));
    }
}

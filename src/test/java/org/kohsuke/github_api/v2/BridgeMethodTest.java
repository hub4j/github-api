package org.kohsuke.github_api.v2;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github_api.v2.GHAppInstallation;
import org.kohsuke.github_api.v2.GHAppInstallationToken;
import org.kohsuke.github_api.v2.GHArtifact;
import org.kohsuke.github_api.v2.GHCheckRun;
import org.kohsuke.github_api.v2.GHCheckSuite;
import org.kohsuke.github_api.v2.GHCommit;
import org.kohsuke.github_api.v2.GHDeployKey;
import org.kohsuke.github_api.v2.GHEventInfo;
import org.kohsuke.github_api.v2.GHEventPayload;
import org.kohsuke.github_api.v2.GHExternalGroup;
import org.kohsuke.github_api.v2.GHIssue;
import org.kohsuke.github_api.v2.GHIssueEvent;
import org.kohsuke.github_api.v2.GHMarketplacePendingChange;
import org.kohsuke.github_api.v2.GHMarketplacePurchase;
import org.kohsuke.github_api.v2.GHMarketplaceUserPurchase;
import org.kohsuke.github_api.v2.GHMilestone;
import org.kohsuke.github_api.v2.GHObject;
import org.kohsuke.github_api.v2.GHPerson;
import org.kohsuke.github_api.v2.GHProjectsV2Item;
import org.kohsuke.github_api.v2.GHProjectsV2ItemChanges;
import org.kohsuke.github_api.v2.GHPullRequest;
import org.kohsuke.github_api.v2.GHPullRequestReview;
import org.kohsuke.github_api.v2.GHRepository;
import org.kohsuke.github_api.v2.GHRepositoryDiscussion;
import org.kohsuke.github_api.v2.GHRepositoryTraffic;
import org.kohsuke.github_api.v2.GHStargazer;
import org.kohsuke.github_api.v2.GHSubscription;
import org.kohsuke.github_api.v2.GHThread;
import org.kohsuke.github_api.v2.GHUser;
import org.kohsuke.github_api.v2.GHWorkflowJob;
import org.kohsuke.github_api.v2.GHWorkflowRun;
import org.kohsuke.github_api.v2.GitCommit;
import org.kohsuke.github_api.v2.GitUser;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class BridgeMethodTest.
 *
 * @author Kohsuke Kawaguchi
 */
public class BridgeMethodTest extends Assert {

    /**
     * Create default BridgeMethodTest instance
     */
    public BridgeMethodTest() {
    }

    /**
     * Test bridge methods.
     */
    @Test
    public void testBridgeMethods() {

        // Some would say this is redundant, given that bridge methods are so thin anyway
        // In the interest of maintaining binary compatibility, we'll do this anyway for a sampling of methods

        // Something odd here
        // verifyBridgeMethods(new GHCommit(), "getAuthor", GHCommit.GHAuthor.class, GitUser.class);
        // verifyBridgeMethods(new GHCommit(), "getCommitter", GHCommit.GHAuthor.class, GitUser.class);

        String artifactId = System.getProperty("test.projectArtifactId", "default");
        // Only run these tests when building the "bridged" artifact
        org.junit.Assume.assumeThat(artifactId, equalTo("github-api-bridged"));

        verifyBridgeMethods(GHAppInstallation.class, "getSuspendedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHAppInstallationToken.class, "getExpiresAt", Date.class, Instant.class);
        verifyBridgeMethods(GHArtifact.class, "getExpiresAt", Date.class, Instant.class);
        verifyBridgeMethods(GHCheckRun.class, "getStartedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHCheckRun.class, "getCompletedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHCheckSuite.HeadCommit.class, "getTimestamp", Date.class, Instant.class);
        verifyBridgeMethods(GHCommit.class, "getAuthoredDate", Date.class, Instant.class);
        verifyBridgeMethods(GHCommit.class, "getCommitDate", Date.class, Instant.class);
        verifyBridgeMethods(GHDeployKey.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHDeployKey.class, "getLastUsedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHEventInfo.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHEventPayload.Push.PushCommit.class, "getTimestamp", Date.class, Instant.class);
        verifyBridgeMethods(GHEventPayload.Star.class, "getStarredAt", Date.class, Instant.class);
        verifyBridgeMethods(GHExternalGroup.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHIssue.class, "getClosedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHIssueEvent.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplacePendingChange.class, "getEffectiveDate", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplacePurchase.class, "getNextBillingDate", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplacePurchase.class, "getFreeTrialEndsOn", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplacePurchase.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplaceUserPurchase.class, "getNextBillingDate", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplaceUserPurchase.class, "getFreeTrialEndsOn", Date.class, Instant.class);
        verifyBridgeMethods(GHMarketplaceUserPurchase.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHMilestone.class, "getDueOn", Date.class, Instant.class);
        verifyBridgeMethods(GHMilestone.class, "getClosedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHObject.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHObject.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHPerson.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHPerson.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHProjectsV2Item.class, "getArchivedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHProjectsV2ItemChanges.FromToDate.class, "getFrom", Date.class, Instant.class);
        verifyBridgeMethods(GHProjectsV2ItemChanges.FromToDate.class, "getTo", Date.class, Instant.class);
        verifyBridgeMethods(GHPullRequest.class, "getMergedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHPullRequestReview.class, "getSubmittedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHPullRequestReview.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHRepository.class, "getPushedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHRepositoryDiscussion.class, "getAnswerChosenAt", Date.class, Instant.class);
        verifyBridgeMethods(GHRepositoryDiscussion.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHRepositoryDiscussion.class, "getUpdatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHRepositoryTraffic.DailyInfo.class, "getTimestamp", Date.class, Instant.class);
        verifyBridgeMethods(GHStargazer.class, "getStarredAt", Date.class, Instant.class);
        verifyBridgeMethods(GHSubscription.class, "getCreatedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHThread.class, "getLastReadAt", Date.class, Instant.class);
        verifyBridgeMethods(GHUser.class, "getSuspendedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowJob.class, "getStartedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowJob.class, "getCompletedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowJob.class, "getStartedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowJob.class, "getCompletedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowRun.class, "getRunStartedAt", Date.class, Instant.class);
        verifyBridgeMethods(GHWorkflowRun.HeadCommit.class, "getTimestamp", Date.class, Instant.class);
        verifyBridgeMethods(GitCommit.class, "getAuthoredDate", Date.class, Instant.class);
        verifyBridgeMethods(GitCommit.class, "getCommitDate", Date.class, Instant.class);
        verifyBridgeMethods(GitUser.class, "getDate", Date.class, Instant.class);
    }

    /**
     * Verify bridge methods.
     *
     * @param targetClass
     *            the target class
     * @param methodName
     *            the method name
     * @param returnTypes
     *            the return types
     */
    void verifyBridgeMethods(@Nonnull Class<?> targetClass, @Nonnull String methodName, Class<?>... returnTypes) {
        verifyBridgeMethods(targetClass, methodName, 0, returnTypes);
    }

    /**
     * Verify bridge methods.
     *
     * @param targetClass
     *            the target class
     * @param methodName
     *            the method name
     * @param parameterCount
     *            the parameter count
     * @param returnTypes
     *            the return types
     */
    void verifyBridgeMethods(@Nonnull Class<?> targetClass,
            @Nonnull String methodName,
            int parameterCount,
            Class<?>... returnTypes) {
        List<Class<?>> foundMethods = new ArrayList<>();
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == parameterCount) {
                foundMethods.add(method.getReturnType());
            }
        }

        assertThat(foundMethods, containsInAnyOrder(returnTypes));
    }
}

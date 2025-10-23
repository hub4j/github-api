package org.kohsuke.github_api.v2;

import org.junit.Test;
import org.kohsuke.github_api.v2.GHCheckRun;
import org.kohsuke.github_api.v2.GHCommentAuthorAssociation;
import org.kohsuke.github_api.v2.GHCommitSearchBuilder;
import org.kohsuke.github_api.v2.GHCommitState;
import org.kohsuke.github_api.v2.GHCompare;
import org.kohsuke.github_api.v2.GHContentSearchBuilder;
import org.kohsuke.github_api.v2.GHDeploymentState;
import org.kohsuke.github_api.v2.GHDirection;
import org.kohsuke.github_api.v2.GHEvent;
import org.kohsuke.github_api.v2.GHFork;
import org.kohsuke.github_api.v2.GHIssueQueryBuilder;
import org.kohsuke.github_api.v2.GHIssueSearchBuilder;
import org.kohsuke.github_api.v2.GHIssueState;
import org.kohsuke.github_api.v2.GHIssueStateReason;
import org.kohsuke.github_api.v2.GHMarketplaceAccountType;
import org.kohsuke.github_api.v2.GHMarketplaceListAccountBuilder;
import org.kohsuke.github_api.v2.GHMarketplacePriceModel;
import org.kohsuke.github_api.v2.GHMembership;
import org.kohsuke.github_api.v2.GHMilestoneState;
import org.kohsuke.github_api.v2.GHMyself;
import org.kohsuke.github_api.v2.GHOrganization;
import org.kohsuke.github_api.v2.GHPermissionType;
import org.kohsuke.github_api.v2.GHProject;
import org.kohsuke.github_api.v2.GHProjectsV2Item;
import org.kohsuke.github_api.v2.GHProjectsV2ItemChanges;
import org.kohsuke.github_api.v2.GHPullRequest;
import org.kohsuke.github_api.v2.GHPullRequestQueryBuilder;
import org.kohsuke.github_api.v2.GHPullRequestReviewComment;
import org.kohsuke.github_api.v2.GHPullRequestReviewEvent;
import org.kohsuke.github_api.v2.GHPullRequestReviewState;
import org.kohsuke.github_api.v2.GHPullRequestSearchBuilder;
import org.kohsuke.github_api.v2.GHReleaseBuilder;
import org.kohsuke.github_api.v2.GHRepository;
import org.kohsuke.github_api.v2.GHRepositoryDiscussion;
import org.kohsuke.github_api.v2.GHRepositorySearchBuilder;
import org.kohsuke.github_api.v2.GHRepositorySelection;
import org.kohsuke.github_api.v2.GHTargetType;
import org.kohsuke.github_api.v2.GHTeam;
import org.kohsuke.github_api.v2.GHUserSearchBuilder;
import org.kohsuke.github_api.v2.GHVerification;
import org.kohsuke.github_api.v2.GHWorkflowRun;
import org.kohsuke.github_api.v2.GitHub;
import org.kohsuke.github_api.v2.MarkdownMode;
import org.kohsuke.github_api.v2.ReactionContent;

import static org.hamcrest.CoreMatchers.*;

// TODO: Auto-generated Javadoc
/**
 * Unit test for {@link GitHub} static helpers.
 *
 * @author Liam Newman
 */
public class EnumTest extends AbstractGitHubWireMockTest {

    /**
     * Create default EnumTest instance
     */
    public EnumTest() {
    }

    /**
     * Touch enums.
     */
    @Test
    public void touchEnums() {
        assertThat(GHCheckRun.AnnotationLevel.values().length, equalTo(3));
        assertThat(GHCheckRun.Conclusion.values().length, equalTo(9));
        assertThat(GHCheckRun.Status.values().length, equalTo(4));

        assertThat(GHCommentAuthorAssociation.values().length, equalTo(9));

        assertThat(GHCommitSearchBuilder.Sort.values().length, equalTo(2));

        assertThat(GHCommitState.values().length, equalTo(4));

        assertThat(GHCompare.Status.values().length, equalTo(4));

        assertThat(GHContentSearchBuilder.Sort.values().length, equalTo(2));

        assertThat(GHDeploymentState.values().length, equalTo(7));

        assertThat(GHDirection.values().length, equalTo(2));

        assertThat(GHEvent.values().length, equalTo(66));
        assertThat(GHEvent.ALL.symbol(), equalTo("*"));
        assertThat(GHEvent.PULL_REQUEST.symbol(), equalTo(GHEvent.PULL_REQUEST.toString().toLowerCase()));

        assertThat(GHFork.values().length, equalTo(3));
        assertThat(GHFork.PARENT_ONLY.toString(), equalTo(""));

        assertThat(GHIssueQueryBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueSearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueState.values().length, equalTo(3));

        assertThat(GHIssueStateReason.values().length, equalTo(4));

        assertThat(GHMarketplaceAccountType.values().length, equalTo(2));

        assertThat(GHMarketplaceListAccountBuilder.Sort.values().length, equalTo(2));

        assertThat(GHMarketplacePriceModel.values().length, equalTo(3));

        assertThat(GHMembership.Role.values().length, equalTo(2));

        assertThat(GHMilestoneState.values().length, equalTo(2));

        assertThat(GHMyself.RepositoryListFilter.values().length, equalTo(5));

        assertThat(GHOrganization.Role.values().length, equalTo(2));
        assertThat(GHOrganization.Permission.values().length, equalTo(6));

        assertThat(GHPermissionType.values().length, equalTo(5));

        assertThat(GHProject.ProjectState.values().length, equalTo(2));
        assertThat(GHProject.ProjectStateFilter.values().length, equalTo(3));

        assertThat(GHProjectsV2Item.ContentType.values().length, equalTo(4));

        assertThat(GHProjectsV2ItemChanges.FieldType.values().length, equalTo(6));

        assertThat(GHPullRequest.MergeMethod.values().length, equalTo(3));

        assertThat(GHPullRequestQueryBuilder.Sort.values().length, equalTo(4));

        assertThat(GHPullRequestReviewComment.Side.values().length, equalTo(3));

        assertThat(GHPullRequestReviewEvent.values().length, equalTo(4));
        assertThat(GHPullRequestReviewEvent.PENDING.toState(), equalTo(GHPullRequestReviewState.PENDING));
        assertThat(GHPullRequestReviewEvent.PENDING.action(), nullValue());

        assertThat(GHPullRequestReviewState.values().length, equalTo(5));
        assertThat(GHPullRequestReviewState.PENDING.toEvent(), equalTo(GHPullRequestReviewEvent.PENDING));
        assertThat(GHPullRequestReviewState.APPROVED.action(), equalTo(GHPullRequestReviewEvent.APPROVE.action()));
        assertThat(GHPullRequestReviewState.DISMISSED.toEvent(), nullValue());

        assertThat(GHPullRequestSearchBuilder.Sort.values().length, equalTo(4));

        assertThat(GHReleaseBuilder.MakeLatest.values().length, equalTo(3));

        assertThat(GHRepository.CollaboratorAffiliation.values().length, equalTo(3));
        assertThat(GHRepository.ForkSort.values().length, equalTo(3));
        assertThat(GHRepository.Visibility.values().length, equalTo(4));

        assertThat(GHRepositoryDiscussion.State.values().length, equalTo(3));

        assertThat(GHRepositorySearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHRepositorySelection.values().length, equalTo(2));

        assertThat(GHTargetType.values().length, equalTo(2));

        assertThat(GHTeam.Role.values().length, equalTo(2));
        assertThat(GHTeam.Privacy.values().length, equalTo(3));

        assertThat(GHUserSearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHVerification.Reason.values().length, equalTo(18));

        assertThat(GHWorkflowRun.Status.values().length, equalTo(15));
        assertThat(GHWorkflowRun.Conclusion.values().length, equalTo(10));

        assertThat(MarkdownMode.values().length, equalTo(2));

        assertThat(ReactionContent.values().length, equalTo(8));
    }
}

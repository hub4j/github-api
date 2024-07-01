package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.internal.Previews;

import static org.hamcrest.CoreMatchers.*;

// TODO: Auto-generated Javadoc
/**
 * Unit test for {@link GitHub} static helpers.
 *
 * @author Liam Newman
 */
public class EnumTest extends AbstractGitHubWireMockTest {

    /**
     * Touch enums.
     */
    @Test
    public void touchEnums() {
        // Previews is deprecated but we want to maintain coverage until we remove it
        assertThat(Previews.values().length, equalTo(16));
        assertThat(Previews.ANTIOPE.mediaType(), equalTo("application/vnd.github.antiope-preview+json"));

        assertThat(GHCheckRun.AnnotationLevel.values().length, equalTo(3));
        assertThat(GHCheckRun.Conclusion.values().length, equalTo(9));
        assertThat(GHCheckRun.Status.values().length, equalTo(4));

        assertThat(GHCommentAuthorAssociation.values().length, equalTo(8));

        assertThat(GHCommitSearchBuilder.Sort.values().length, equalTo(2));

        assertThat(GHCommitState.values().length, equalTo(4));

        assertThat(GHCompare.Status.values().length, equalTo(4));

        assertThat(GHContentSearchBuilder.Sort.values().length, equalTo(2));

        assertThat(GHDeploymentState.values().length, equalTo(7));

        assertThat(GHDirection.values().length, equalTo(2));

        assertThat(GHEvent.values().length, equalTo(65));
        assertThat(GHEvent.ALL.symbol(), equalTo("*"));
        assertThat(GHEvent.PULL_REQUEST.symbol(), equalTo(GHEvent.PULL_REQUEST.toString().toLowerCase()));

        assertThat(GHFork.values().length, equalTo(3));
        assertThat(GHFork.PARENT_ONLY.toString(), equalTo(""));

        assertThat(GHIssueQueryBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueSearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueState.values().length, equalTo(3));

        assertThat(GHIssueStateReason.values().length, equalTo(3));

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

        assertThat(GHPullRequestReviewState.values().length, equalTo(6));
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
        assertThat(GHRepositorySearchBuilder.Fork.values().length, equalTo(3));
        assertThat(GHRepositorySearchBuilder.Fork.PARENT_ONLY.toString(), equalTo(""));

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

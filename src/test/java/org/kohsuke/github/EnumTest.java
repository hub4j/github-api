package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHPullRequest.MergeMethod;

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
        assertThat(GHCheckRun.AnnotationLevel.values().length, equalTo(3));
        assertThat(GHCheckRun.Conclusion.values().length, equalTo(9));
        assertThat(GHCheckRun.Status.values().length, equalTo(4));

        assertThat(GHCommentAuthorAssociation.values().length, equalTo(7));

        assertThat(GHCommitState.values().length, equalTo(4));

        assertThat(GHCompare.Status.values().length, equalTo(4));

        assertThat(GHDeploymentState.values().length, equalTo(7));

        assertThat(GHDirection.values().length, equalTo(2));

        assertThat(GHEvent.values().length, equalTo(64));
        assertThat(GHEvent.ALL.symbol(), equalTo("*"));
        assertThat(GHEvent.PULL_REQUEST.symbol(), equalTo(GHEvent.PULL_REQUEST.toString().toLowerCase()));

        assertThat(GHIssueSearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueState.values().length, equalTo(3));

        assertThat(GHMarketplaceAccountType.values().length, equalTo(2));

        assertThat(GHMarketplaceListAccountBuilder.Sort.values().length, equalTo(2));

        assertThat(GHMarketplacePriceModel.values().length, equalTo(3));

        assertThat(GHMembership.Role.values().length, equalTo(2));

        assertThat(GHMilestoneState.values().length, equalTo(2));

        assertThat(GHMyself.RepositoryListFilter.values().length, equalTo(5));

        assertThat(GHOrganization.Role.values().length, equalTo(2));
        assertThat(GHOrganization.Permission.values().length, equalTo(5));

        assertThat(GHPermissionType.values().length, equalTo(4));

        assertThat(GHProject.ProjectState.values().length, equalTo(2));
        assertThat(GHProject.ProjectStateFilter.values().length, equalTo(3));

        assertThat(MergeMethod.values().length, equalTo(3));

        assertThat(GHPullRequestQueryBuilder.Sort.values().length, equalTo(4));

        assertThat(GHPullRequestReviewEvent.values().length, equalTo(4));
        assertThat(GHPullRequestReviewEvent.PENDING.toState(), equalTo(GHPullRequestReviewState.PENDING));
        assertThat(GHPullRequestReviewEvent.PENDING.action(), nullValue());

        assertThat(GHPullRequestReviewState.values().length, equalTo(6));
        assertThat(GHPullRequestReviewState.PENDING.toEvent(), equalTo(GHPullRequestReviewEvent.PENDING));
        assertThat(GHPullRequestReviewState.APPROVED.action(), equalTo(GHPullRequestReviewEvent.APPROVE.action()));
        assertThat(GHPullRequestReviewState.DISMISSED.toEvent(), nullValue());

        assertThat(GHRepository.CollaboratorAffiliation.values().length, equalTo(3));
        assertThat(GHRepository.ForkSort.values().length, equalTo(3));
        assertThat(GHRepository.Visibility.values().length, equalTo(4));

        assertThat(GHRepositorySearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHRepositorySelection.values().length, equalTo(2));

        assertThat(GHTeam.Role.values().length, equalTo(2));
        assertThat(GHTeam.Privacy.values().length, equalTo(2));

        assertThat(GHUserSearchBuilder.Sort.values().length, equalTo(3));

        assertThat(GHIssueQueryBuilder.Sort.values().length, equalTo(3));
    }

}

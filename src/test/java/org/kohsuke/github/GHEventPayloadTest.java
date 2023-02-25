package org.kohsuke.github;

import org.junit.Rule;
import org.junit.Test;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHProjectsV2Item.ContentType;
import org.kohsuke.github.GHProjectsV2ItemChanges.FieldType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;

// TODO: Auto-generated Javadoc
/**
 * The Class GHEventPayloadTest.
 */
public class GHEventPayloadTest extends AbstractGitHubWireMockTest {

    /** The payload. */
    @Rule
    public final PayloadRule payload = new PayloadRule(".json");

    /**
     * Instantiates a new GH event payload test.
     */
    public GHEventPayloadTest() {
        useDefaultGitHub = false;
    }

    /**
     * Commit comment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void commit_comment() throws Exception {
        final GHEventPayload.CommitComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.CommitComment.class);
        assertThat(event.getAction(), is("created"));
        assertThat(event.getComment().getSHA1(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getComment().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getComment().getOwner(), sameInstance(event.getRepository()));

        assertThrows(RuntimeException.class, () -> event.setComment(null));

        // EventPayload checks
        assertThrows(RuntimeException.class, () -> event.setOrganization(null));
        assertThrows(RuntimeException.class, () -> event.setRepository(null));
        assertThrows(RuntimeException.class, () -> event.setSender(null));
    }

    /**
     * Creates the.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void create() throws Exception {
        final GHEventPayload.Create event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Create.class);
        assertThat(event.getRef(), is("0.0.1"));
        assertThat(event.getRefType(), is("tag"));
        assertThat(event.getMasterBranch(), is("main"));
        assertThat(event.getDescription(), is(""));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    /**
     * Delete.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void delete() throws Exception {
        final GHEventPayload.Delete event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Delete.class);
        assertThat(event.getRef(), is("simple-tag"));
        assertThat(event.getRefType(), is("tag"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    /**
     * Deployment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void deployment() throws Exception {
        final GHEventPayload.Deployment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Deployment.class);
        assertThat(event.getDeployment().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getDeployment().getEnvironment(), is("production"));
        assertThat(event.getDeployment().getCreator().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getDeployment().getOwner(), sameInstance(event.getRepository()));
    }

    /**
     * Deployment status.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void deployment_status() throws Exception {
        final GHEventPayload.DeploymentStatus event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.DeploymentStatus.class);
        assertThat(event.getDeploymentStatus().getState(), is(GHDeploymentState.SUCCESS));
        assertThat(event.getDeploymentStatus().getTargetUrl(), nullValue());
        assertThat(event.getDeployment().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getDeployment().getEnvironment(), is("production"));
        assertThat(event.getDeployment().getCreator().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getDeployment().getOwner(), sameInstance(event.getRepository()));
        assertThat(event.getDeploymentStatus().getOwner(), sameInstance(event.getRepository()));

        assertThrows(RuntimeException.class, () -> event.setDeployment(null));
        assertThrows(RuntimeException.class, () -> event.setDeploymentStatus(null));
    }

    /**
     * Fork.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void fork() throws Exception {
        final GHEventPayload.Fork event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Fork.class);
        assertThat(event.getForkee().getName(), is("public-repo"));
        assertThat(event.getForkee().getOwner().getLogin(), is("baxterandthehackers"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterandthehackers"));

        assertThrows(RuntimeException.class, () -> event.setForkee(null));
    }

    // TODO uncomment when we have GHPage implemented
    // @Test
    // public void gollum() throws Exception {
    // GHEventPayload.Gollum event =
    // GitHub.offline().parseEventPayload(payload.asReader(), GHEventPayload.Gollum.class);
    // assertThat(event.getPages().size(), is(1));
    // GHPage page = event.getPages().get(0);
    // assertThat(page.getName(), is("Home"));
    // assertThat(page.getTitle(), is("Home"));
    // assertThat(page.getSummary(), nullValue());
    // assertThat(page.getAction(), is("created"));
    // assertThat(page.getSha(), is("91ea1bd42aa2ba166b86e8aefe049e9837214e67"));
    // assertThat(page.getHtmlUrl(), is("https://github.com/baxterthehacker/public-repo/wiki/Home"));
    // assertThat(event.getRepository().getName(), is("public-repo"));
    // assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
    // assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    // }

    /**
     * Issue comment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_comment() throws Exception {
        final GHEventPayload.IssueComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.IssueComment.class);
        assertThat(event.getAction(), is("created"));
        assertThat(event.getIssue().getNumber(), is(2));
        assertThat(event.getIssue().getTitle(), is("Spelling error in the README file"));
        assertThat(event.getIssue().getState(), is(GHIssueState.OPEN));
        assertThat(event.getIssue().getLabels().size(), is(1));
        assertThat(event.getIssue().getLabels().iterator().next().getName(), is("bug"));
        assertThat(event.getComment().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getComment().getBody(), is("You are totally right! I'll get this fixed right away."));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getIssue().getRepository(), sameInstance(event.getRepository()));
        assertThat(event.getComment().getParent(), sameInstance(event.getIssue()));

        assertThrows(RuntimeException.class, () -> event.setComment(null));
        assertThrows(RuntimeException.class, () -> event.setIssue(null));
    }

    /**
     * Issue comment edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_comment_edited() throws Exception {
        final GHEventPayload.IssueComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.IssueComment.class);
        assertThat(event.getAction(), is("edited"));
        assertThat(event.getComment().getBody(), is("This is the issue comment AFTER edit."));
        assertThat(event.getChanges().getBody().getFrom(), is("This is the issue comment BEFORE edit."));
    }

    /**
     * Issues.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issues() throws Exception {
        final GHEventPayload.Issue event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("opened"));
        assertThat(event.getIssue().getNumber(), is(2));
        assertThat(event.getIssue().getTitle(), is("Spelling error in the README file"));
        assertThat(event.getIssue().getState(), is(GHIssueState.OPEN));
        assertThat(event.getIssue().getLabels().size(), is(1));
        assertThat(event.getIssue().getLabels().iterator().next().getName(), is("bug"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getIssue().getRepository(), sameInstance(event.getRepository()));
    }

    /**
     * Issue labeled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_labeled() throws Exception {
        final GHEventPayload.Issue event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("labeled"));
        assertThat(event.getIssue().getNumber(), is(42));
        assertThat(event.getIssue().getTitle(), is("Test GHEventPayload.Issue label/unlabel"));
        assertThat(event.getIssue().getLabels().size(), is(1));
        assertThat(event.getIssue().getLabels().iterator().next().getName(), is("enhancement"));
        assertThat(event.getLabel().getName(), is("enhancement"));
    }

    /**
     * Issue unlabeled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_unlabeled() throws Exception {
        final GHEventPayload.Issue event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("unlabeled"));
        assertThat(event.getIssue().getNumber(), is(42));
        assertThat(event.getIssue().getTitle(), is("Test GHEventPayload.Issue label/unlabel"));
        assertThat(event.getIssue().getLabels().size(), is(0));
        assertThat(event.getLabel().getName(), is("enhancement"));
    }

    /**
     * Issue title edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_title_edited() throws Exception {
        final GHEventPayload.Issue event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("edited"));
        assertThat(event.getIssue().getNumber(), is(43));
        assertThat(event.getIssue().getTitle(), is("Test GHEventPayload.Issue changes [updated]"));
        assertThat(event.getChanges().getTitle().getFrom(), is("Test GHEventPayload.Issue changes"));
    }

    /**
     * Issue body edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issue_body_edited() throws Exception {
        final GHEventPayload.Issue event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("edited"));
        assertThat(event.getIssue().getNumber(), is(43));
        assertThat(event.getIssue().getBody(), is("Description [updated]."));
        assertThat(event.getChanges().getBody().getFrom(), is("Description."));
    }

    // TODO implement support classes and write test
    // @Test
    // public void label() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void member() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void membership() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void milestone() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void page_build() throws Exception {}

    /**
     * Ping.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void ping() throws Exception {
        final GHEventPayload.Ping event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Ping.class);

        assertThat(event.getAction(), nullValue());
        assertThat(event.getSender().getLogin(), is("seregamorph"));
        assertThat(event.getRepository().getName(), is("acme-project-project"));
        assertThat(event.getOrganization(), nullValue());
    }

    /**
     * Public.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("public")
    public void public_() throws Exception {
        final GHEventPayload.Public event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Public.class);
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    /**
     * Pull request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request() throws Exception {
        final GHEventPayload.PullRequest event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequest.class);
        assertThat(event.getAction(), is("opened"));
        assertThat(event.getNumber(), is(1));
        assertThat(event.getPullRequest().getNumber(), is(1));
        assertThat(event.getPullRequest().getTitle(), is("Update the README with new information"));
        assertThat(event.getPullRequest().getBody(),
                is("This is a pretty simple change that we need to pull into " + "main."));
        assertThat(event.getPullRequest().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getRef(), is("changes"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("baxterthehacker:changes"));
        assertThat(event.getPullRequest().getHead().getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("main"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:main"));
        assertThat(event.getPullRequest().getBase().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getPullRequest().isMerged(), is(false));
        assertThat(event.getPullRequest().getMergeable(), nullValue());
        assertThat(event.getPullRequest().getMergeableState(), is("unknown"));
        assertThat(event.getPullRequest().getMergedBy(), nullValue());
        assertThat(event.getPullRequest().getCommentsCount(), is(0));
        assertThat(event.getPullRequest().getReviewComments(), is(0));
        assertThat(event.getPullRequest().getAdditions(), is(1));
        assertThat(event.getPullRequest().getDeletions(), is(1));
        assertThat(event.getPullRequest().getChangedFiles(), is(1));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getPullRequest().getRepository(), sameInstance(event.getRepository()));
    }

    /**
     * Pull request edited base.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_edited_base() throws Exception {
        final GHEventPayload.PullRequest event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequest.class);

        assertThat(event.getAction(), is("edited"));
        assertThat(event.getChanges().getTitle(), nullValue());
        assertThat(event.getPullRequest().getTitle(), is("REST-276 - easy-random"));
        assertThat(event.getChanges().getBase().getRef().getFrom(), is("develop"));
        assertThat(event.getChanges().getBase().getSha().getFrom(), is("4b0f3b9fd582b071652ccfccd10bfc8c143cff96"));
        assertThat(event.getPullRequest().getBase().getRef(), is("4.3"));
        assertThat(event.getPullRequest().getBody(), startsWith("**JIRA Ticket URL:**"));
        assertThat(event.getChanges().getBody(), nullValue());
    }

    /**
     * Pull request edited title.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_edited_title() throws Exception {
        final GHEventPayload.PullRequest event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequest.class);

        assertThat(event.getAction(), is("edited"));
        assertThat(event.getChanges().getTitle().getFrom(), is("REST-276 - easy-random"));
        assertThat(event.getPullRequest().getTitle(), is("REST-276 - easy-random 4.3.0"));
        assertThat(event.getChanges().getBase(), nullValue());
        assertThat(event.getPullRequest().getBase().getRef(), is("4.3"));
        assertThat(event.getPullRequest().getBody(), startsWith("**JIRA Ticket URL:**"));
        assertThat(event.getChanges().getBody(), nullValue());
    }

    /**
     * Pull request labeled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_labeled() throws Exception {
        final GHEventPayload.PullRequest event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequest.class);
        assertThat(event.getAction(), is("labeled"));
        assertThat(event.getNumber(), is(79));
        assertThat(event.getPullRequest().getNumber(), is(79));
        assertThat(event.getPullRequest().getTitle(), is("Base POJO test enhancement"));
        assertThat(event.getPullRequest().getBody(),
                is("This is a pretty simple change that we need to pull into develop."));
        assertThat(event.getPullRequest().getUser().getLogin(), is("seregamorph"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("trilogy-group"));
        assertThat(event.getPullRequest().getHead().getRef(), is("changes"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("trilogy-group:changes"));
        assertThat(event.getPullRequest().getHead().getSha(), is("4b91e3a970fb967fb7be4d52e0969f8e3fb063d0"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("trilogy-group"));
        assertThat(event.getPullRequest().getBase().getRef(), is("3.10"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("trilogy-group:3.10"));
        assertThat(event.getPullRequest().getBase().getSha(), is("7a735f17d686c6a1fc7df5b9d395e5863868f364"));
        assertThat(event.getPullRequest().isMerged(), is(false));
        assertThat(event.getPullRequest().getMergeable(), is(true));
        assertThat(event.getPullRequest().getMergeableState(), is("draft"));
        assertThat(event.getPullRequest().getMergedBy(), nullValue());
        assertThat(event.getPullRequest().getCommentsCount(), is(1));
        assertThat(event.getPullRequest().getReviewComments(), is(14));
        assertThat(event.getPullRequest().getAdditions(), is(137));
        assertThat(event.getPullRequest().getDeletions(), is(81));
        assertThat(event.getPullRequest().getChangedFiles(), is(22));
        assertThat(event.getPullRequest().getLabels().iterator().next().getName(), is("Ready for Review"));
        assertThat(event.getRepository().getName(), is("trilogy-rest-api-framework"));
        assertThat(event.getRepository().getOwner().getLogin(), is("trilogy-group"));
        assertThat(event.getSender().getLogin(), is("schernov-xo"));
        assertThat(event.getLabel().getUrl(),
                is("https://api.github.com/repos/trilogy-group/trilogy-rest-api-framework/labels/rest%20api"));
        assertThat(event.getLabel().getName(), is("rest api"));
        assertThat(event.getLabel().getColor(), is("fef2c0"));
        assertThat(event.getLabel().getDescription(), is("REST API pull request"));
        assertThat(event.getOrganization().getLogin(), is("trilogy-group"));
    }

    /**
     * Pull request review.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_review() throws Exception {
        final GHEventPayload.PullRequestReview event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequestReview.class);
        assertThat(event.getAction(), is("submitted"));

        assertThat(event.getReview().getId(), is(2626884L));
        assertThat(event.getReview().getBody(), is("Looks great!\n"));
        assertThat(event.getReview().getState(), is(GHPullRequestReviewState.APPROVED));

        assertThat(event.getPullRequest().getNumber(), is(8));
        assertThat(event.getPullRequest().getTitle(), is("Add a README description"));
        assertThat(event.getPullRequest().getBody(), is("Just a few more details"));
        assertThat(event.getReview().getHtmlUrl(),
                hasToString("https://github.com/baxterthehacker/public-repo/pull/8#pullrequestreview-2626884"));
        assertThat(event.getPullRequest().getUser().getLogin(), is("skalnik"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("skalnik"));
        assertThat(event.getPullRequest().getHead().getRef(), is("patch-2"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("skalnik:patch-2"));
        assertThat(event.getPullRequest().getHead().getSha(), is("b7a1f9c27caa4e03c14a88feb56e2d4f7500aa63"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("main"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:main"));
        assertThat(event.getPullRequest().getBase().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));

        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));

        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getPullRequest().getRepository(), sameInstance(event.getRepository()));
        assertThat(event.getReview().getParent(), sameInstance(event.getPullRequest()));
    }

    /**
     * Pull request review comment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_review_comment() throws Exception {
        final GHEventPayload.PullRequestReviewComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequestReviewComment.class);
        assertThat(event.getAction(), is("created"));

        assertThat(event.getComment().getBody(), is("Maybe you should use more emojji on this line."));

        assertThat(event.getPullRequest().getNumber(), is(1));
        assertThat(event.getPullRequest().getTitle(), is("Update the README with new information"));
        assertThat(event.getPullRequest().getBody(),
                is("This is a pretty simple change that we need to pull into main."));
        assertThat(event.getPullRequest().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getRef(), is("changes"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("baxterthehacker:changes"));
        assertThat(event.getPullRequest().getHead().getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("main"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:main"));
        assertThat(event.getPullRequest().getBase().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));

        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));

        assertThat(event.getSender().getLogin(), is("baxterthehacker"));

        assertThat(event.getPullRequest().getRepository(), sameInstance(event.getRepository()));
        assertThat(event.getComment().getParent(), sameInstance(event.getPullRequest()));
    }

    /**
     * Pull request review comment edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pull_request_review_comment_edited() throws Exception {
        final GHEventPayload.PullRequestReviewComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequestReviewComment.class);
        assertThat(event.getAction(), is("edited"));
        assertThat(event.getPullRequest().getNumber(), is(4));
        assertThat(event.getComment().getBody(), is("This is the pull request review comment AFTER edit."));
        assertThat(event.getChanges().getBody().getFrom(), is("This is the pull request review comment BEFORE edit."));
    }

    /**
     * Push.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void push() throws Exception {
        final GHEventPayload.Push event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Push.class);
        assertThat(event.getRef(), is("refs/heads/changes"));
        assertThat(event.getBefore(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getHead(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.isCreated(), is(false));
        assertThat(event.isDeleted(), is(false));
        assertThat(event.isForced(), is(false));
        assertThat(event.getCommits().size(), is(1));
        assertThat(event.getCommits().get(0).getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getCommits().get(0).getAuthor().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getCommits().get(0).getAuthor().getUsername(), is("baxterthehacker"));
        assertThat(event.getCommits().get(0).getCommitter().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getCommits().get(0).getCommitter().getUsername(), is("baxterthehacker"));
        assertThat(event.getCommits().get(0).getAdded().size(), is(0));
        assertThat(event.getCommits().get(0).getRemoved().size(), is(0));
        assertThat(event.getCommits().get(0).getModified().size(), is(1));
        assertThat(event.getCommits().get(0).getModified().get(0), is("README.md"));

        assertThat(event.getHeadCommit().getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getHeadCommit().getAuthor().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getHeadCommit().getAuthor().getUsername(), is("baxterthehacker"));
        assertThat(event.getHeadCommit().getCommitter().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getHeadCommit().getCommitter().getUsername(), is("baxterthehacker"));
        assertThat(event.getHeadCommit().getAdded().size(), is(0));
        assertThat(event.getHeadCommit().getRemoved().size(), is(0));
        assertThat(event.getHeadCommit().getModified().size(), is(1));
        assertThat(event.getHeadCommit().getModified().get(0), is("README.md"));
        assertThat(event.getHeadCommit().getMessage(), is("Update README.md"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(event.getCommits().get(0).getTimestamp()), is("2015-05-05T23:40:15Z"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwnerName(), is("baxterthehacker"));
        assertThat(event.getRepository().getUrl().toExternalForm(),
                is("https://github.com/baxterthehacker/public-repo"));
        assertThat(event.getPusher().getName(), is("baxterthehacker"));
        assertThat(event.getPusher().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
        assertThat(event.getCompare(),
                is("https://github.com/baxterthehacker/public-repo/compare/9049f1265b7d...0d1a26e67d8f"));

        assertThrows(RuntimeException.class, () -> event.setPusher(null));
        assertThrows(RuntimeException.class, () -> event.getPusher().setEmail(null));
        assertThrows(RuntimeException.class, () -> event.getPusher().setName(null));

    }

    /**
     * Push to fork.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("push.fork")
    public void pushToFork() throws Exception {
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        final GHEventPayload.Push event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Push.class);
        assertThat(event.getRef(), is("refs/heads/changes"));
        assertThat(event.getBefore(), is("85c44b352958bf6d81b74ab8b21920f1d313a287"));
        assertThat(event.getHead(), is("1393706f1364742defbc28ba459082630ca979af"));
        assertThat(event.isCreated(), is(false));
        assertThat(event.isDeleted(), is(false));
        assertThat(event.isForced(), is(false));
        assertThat(event.getCommits().size(), is(1));
        assertThat(event.getCommits().get(0).getSha(), is("1393706f1364742defbc28ba459082630ca979af"));
        assertThat(event.getCommits().get(0).getAuthor().getEmail(), is("bitwiseman@gmail.com"));
        assertThat(event.getCommits().get(0).getCommitter().getEmail(), is("bitwiseman@gmail.com"));
        assertThat(event.getCommits().get(0).getAdded().size(), is(6));
        assertThat(event.getCommits().get(0).getRemoved().size(), is(0));
        assertThat(event.getCommits().get(0).getModified().size(), is(2));
        assertThat(event.getCommits().get(0).getModified().get(0),
                is("src/main/java/org/kohsuke/github/GHLicense.java"));
        assertThat(event.getRepository().getName(), is("github-api"));
        assertThat(event.getRepository().getOwnerName(), is("hub4j-test-org"));
        assertThat(event.getRepository().getUrl().toExternalForm(), is("https://github.com/hub4j-test-org/github-api"));
        assertThat(event.getPusher().getName(), is("bitwiseman"));
        assertThat(event.getPusher().getEmail(), is("bitwiseman@gmail.com"));
        assertThat(event.getSender().getLogin(), is("bitwiseman"));

        assertThat(event.getRepository().isFork(), is(true));

        // in offliine mode, we should not populate missing fields
        assertThat(event.getRepository().getSource(), is(nullValue()));
        assertThat(event.getRepository().getParent(), is(nullValue()));

        assertThat(event.getRepository().getUrl().toString(), is("https://github.com/hub4j-test-org/github-api"));
        assertThat(event.getRepository().getHttpTransportUrl().toString(),
                is("https://github.com/hub4j-test-org/github-api.git"));

        // Test repository populate
        final GHEventPayload.Push event2 = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub),
                GHEventPayload.Push.class);
        assertThat(event2.getRepository().getUrl().toString(), is("https://github.com/hub4j-test-org/github-api"));
        assertThat(event2.getRepository().getHttpTransportUrl(),
                is("https://github.com/hub4j-test-org/github-api.git"));

        event2.getRepository().populate();

        // After populate the url is fixed to point to the correct API endpoint
        assertThat(event2.getRepository().getUrl().toString(),
                is(mockGitHub.apiServer().baseUrl() + "/repos/hub4j-test-org/github-api"));
        assertThat(event2.getRepository().getHttpTransportUrl(),
                is("https://github.com/hub4j-test-org/github-api.git"));

        // ensure that root has been bound after populate
        event2.getRepository().getSource().getRef("heads/main");
        event2.getRepository().getParent().getRef("heads/main");

        // Source
        final GHEventPayload.Push event3 = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub),
                GHEventPayload.Push.class);
        assertThat(event3.getRepository().getSource().getFullName(), is("hub4j/github-api"));

        // Parent
        final GHEventPayload.Push event4 = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub),
                GHEventPayload.Push.class);
        assertThat(event4.getRepository().getParent().getFullName(), is("hub4j/github-api"));

    }

    /**
     * Release published.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void release_published() throws Exception {
        final GHEventPayload.Release event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Release.class);

        assertThat(event.getAction(), is("published"));
        assertThat(event.getSender().getLogin(), is("seregamorph"));
        assertThat(event.getRepository().getName(), is("company-rest-api-framework"));
        assertThat(event.getOrganization().getLogin(), is("company-group"));
        assertThat(event.getInstallation(), nullValue());
        assertThat(event.getRelease().getName(), is("4.2"));
        assertThat(event.getRelease().getTagName(), is("rest-api-framework-4.2"));
        assertThat(event.getRelease().getBody(), is("REST-269 - unique test executions (#86) Sergey Chernov"));

        assertThrows(RuntimeException.class, () -> event.setRelease(null));
    }

    /**
     * Repository.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void repository() throws Exception {
        final GHEventPayload.Repository event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Repository.class);
        assertThat(event.getAction(), is("created"));
        assertThat(event.getRepository().getName(), is("new-repository"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterandthehackers"));
        assertThat(event.getOrganization().getLogin(), is("baxterandthehackers"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    /**
     * Status.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void status() throws Exception {
        final GHEventPayload.Status event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Status.class);
        assertThat(event.getContext(), is("default"));
        assertThat(event.getDescription(), is("status description"));
        assertThat(event.getState(), is(GHCommitState.SUCCESS));
        assertThat(event.getCommit().getSHA1(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getTargetUrl(), nullValue());
        assertThat(event.getCommit().getOwner(), sameInstance(event.getRepository()));

        assertThrows(RuntimeException.class, () -> event.setCommit(null));
        assertThrows(RuntimeException.class, () -> event.setState(GHCommitState.ERROR));
    }

    /**
     * Status 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void status2() throws Exception {
        final GHEventPayload.Status event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Status.class);
        assertThat(event.getTargetUrl(), is("https://www.wikipedia.org/"));

        assertThat(event.getCommit().getOwner(), sameInstance(event.getRepository()));
    }

    // TODO implement support classes and write test
    // @Test
    // public void team_add() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void watch() throws Exception {}

    /**
     * Check run event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("check-run")
    public void checkRunEvent() throws Exception {
        final GHEventPayload.CheckRun event = GitHub.offline()
                .parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub), GHEventPayload.CheckRun.class);
        final GHCheckRun checkRun = verifyBasicCheckRunEvent(event);
        assertThat("pull body not populated offline", checkRun.getPullRequests().get(0).getBody(), nullValue());
        assertThat("using offline github", mockGitHub.getRequestCount(), equalTo(0));
        assertThat(checkRun.getPullRequests().get(0).getRepository(), sameInstance(event.getRepository()));

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        final GHEventPayload.CheckRun event2 = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub),
                GHEventPayload.CheckRun.class);
        final GHCheckRun checkRun2 = verifyBasicCheckRunEvent(event2);

        int expectedRequestCount = 2;
        assertThat("pull body should be populated",
                checkRun2.getPullRequests().get(0).getBody(),
                equalTo("This is a pretty simple change that we need to pull into main."));
        assertThat("multiple getPullRequests() calls are made, the pull is populated only once",
                mockGitHub.getRequestCount(),
                equalTo(expectedRequestCount));
    }

    private GHCheckRun verifyBasicCheckRunEvent(final GHEventPayload.CheckRun event) throws IOException {
        assertThat(event.getRepository().getName(), is("Hello-World"));
        assertThat(event.getRepository().getOwner().getLogin(), is("Codertocat"));
        assertThat(event.getAction(), is("created"));
        assertThat(event.getRequestedAction(), nullValue());
        assertThrows(RuntimeException.class, () -> event.setCheckRun(null));
        assertThrows(RuntimeException.class, () -> event.setRequestedAction(null));

        // Checks the deserialization of check_run
        final GHCheckRun checkRun = event.getCheckRun();
        assertThat(checkRun.getName(), is("Octocoders-linter"));
        assertThat(checkRun.getHeadSha(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkRun.getStatus(), is(Status.COMPLETED));
        assertThat(checkRun.getNodeId(), is("MDg6Q2hlY2tSdW4xMjg2MjAyMjg="));
        assertThat(checkRun.getExternalId(), is(""));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(checkRun.getStartedAt()), is("2019-05-15T15:21:12Z"));
        assertThat(formatter.format(checkRun.getCompletedAt()), is("2019-05-15T20:22:22Z"));

        assertThat(checkRun.getConclusion(), is(Conclusion.SUCCESS));
        assertThat(checkRun.getUrl().toString(), endsWith("/repos/Codertocat/Hello-World/check-runs/128620228"));
        assertThat(checkRun.getHtmlUrl().toString(),
                endsWith("https://github.com/Codertocat/Hello-World/runs/128620228"));
        assertThat(checkRun.getDetailsUrl().toString(), is("https://octocoders.io"));
        assertThat(checkRun.getApp().getId(), is(29310L));
        assertThat(checkRun.getCheckSuite().getId(), is(118578147L));
        assertThat(checkRun.getOutput().getTitle(), is("check-run output"));
        assertThat(checkRun.getOutput().getSummary(), nullValue());
        assertThat(checkRun.getOutput().getText(), nullValue());
        assertThat(checkRun.getOutput().getAnnotationsCount(), is(0));
        assertThat(checkRun.getOutput().getAnnotationsUrl().toString(),
                endsWith("/repos/Codertocat/Hello-World/check-runs/128620228/annotations"));

        // Checks the deserialization of sender
        assertThat(event.getSender().getId(), is(21031067L));

        assertThat(checkRun.getPullRequests(), notNullValue());
        assertThat(checkRun.getPullRequests().size(), equalTo(1));
        assertThat(checkRun.getPullRequests().get(0).getNumber(), equalTo(2));
        return checkRun;
    }

    /**
     * Check suite event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("check-suite")
    public void checkSuiteEvent() throws Exception {
        final GHEventPayload.CheckSuite event = GitHub.offline()
                .parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub), GHEventPayload.CheckSuite.class);
        final GHCheckSuite checkSuite = verifyBasicCheckSuiteEvent(event);
        assertThat("pull body not populated offline", checkSuite.getPullRequests().get(0).getBody(), nullValue());
        assertThat("using offline github", mockGitHub.getRequestCount(), equalTo(0));
        assertThat(checkSuite.getPullRequests().get(0).getRepository(), sameInstance(event.getRepository()));

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        final GHEventPayload.CheckSuite event2 = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub),
                GHEventPayload.CheckSuite.class);
        final GHCheckSuite checkSuite2 = verifyBasicCheckSuiteEvent(event2);

        int expectedRequestCount = mockGitHub.isUseProxy() ? 3 : 2;
        assertThat("pull body should be populated",
                checkSuite2.getPullRequests().get(0).getBody(),
                equalTo("This is a pretty simple change that we need to pull into main."));
        assertThat("multiple getPullRequests() calls are made, the pull is populated only once",
                mockGitHub.getRequestCount(),
                lessThanOrEqualTo(expectedRequestCount));
    }

    private GHCheckSuite verifyBasicCheckSuiteEvent(final GHEventPayload.CheckSuite event) throws IOException {
        assertThat(event.getRepository().getName(), is("Hello-World"));
        assertThat(event.getRepository().getOwner().getLogin(), is("Codertocat"));
        assertThat(event.getAction(), is("completed"));
        assertThat(event.getSender().getId(), is(21031067L));

        // Checks the deserialization of check_suite
        final GHCheckSuite checkSuite = event.getCheckSuite();
        assertThat(checkSuite.getNodeId(), is("MDEwOkNoZWNrU3VpdGUxMTg1NzgxNDc="));
        assertThat(checkSuite.getHeadBranch(), is("changes"));
        assertThat(checkSuite.getHeadSha(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getStatus(), is("completed"));
        assertThat(checkSuite.getConclusion(), is("success"));
        assertThat(checkSuite.getBefore(), is("6113728f27ae82c7b1a177c8d03f9e96e0adf246"));
        assertThat(checkSuite.getAfter(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getLatestCheckRunsCount(), is(1));
        assertThat(checkSuite.getCheckRunsUrl().toString(),
                endsWith("/repos/Codertocat/Hello-World/check-suites/118578147/check-runs"));
        assertThat(checkSuite.getHeadCommit().getMessage(), is("Update README.md"));
        assertThat(checkSuite.getHeadCommit().getId(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getHeadCommit().getTreeId(), is("31b122c26a97cf9af023e9ddab94a82c6e77b0ea"));
        assertThat(checkSuite.getHeadCommit().getAuthor().getName(), is("Codertocat"));
        assertThat(checkSuite.getHeadCommit().getCommitter().getName(), is("Codertocat"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(checkSuite.getHeadCommit().getTimestamp()), is("2019-05-15T15:20:30Z"));

        assertThat(checkSuite.getApp().getId(), is(29310L));

        assertThat(checkSuite.getPullRequests(), notNullValue());
        assertThat(checkSuite.getPullRequests().size(), equalTo(1));
        assertThat(checkSuite.getPullRequests().get(0).getNumber(), equalTo(2));
        return checkSuite;
    }

    /**
     * Installation repositories event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("installation_repositories")
    public void InstallationRepositoriesEvent() throws Exception {
        final GHEventPayload.InstallationRepositories event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.InstallationRepositories.class);

        assertThat(event.getAction(), is("added"));
        assertThat(event.getInstallation().getId(), is(957387L));
        assertThat(event.getInstallation().getAccount().getLogin(), is("Codertocat"));
        assertThat(event.getRepositorySelection(), is("selected"));

        assertThat(event.getRepositoriesAdded().get(0).getId(), is(186853007L));
        assertThat(event.getRepositoriesAdded().get(0).getNodeId(), is("MDEwOlJlcG9zaXRvcnkxODY4NTMwMDc="));
        assertThat(event.getRepositoriesAdded().get(0).getName(), is("Space"));
        assertThat(event.getRepositoriesAdded().get(0).getFullName(), is("Codertocat/Space"));
        assertThat(event.getRepositoriesAdded().get(0).isPrivate(), is(false));

        assertThat(event.getRepositoriesRemoved(), is(Collections.emptyList()));
        assertThat(event.getSender().getLogin(), is("Codertocat"));
    }

    /**
     * Installation event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("installation")
    public void InstallationEvent() throws Exception {
        final GHEventPayload.Installation event = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .build()
                .parseEventPayload(payload.asReader(), GHEventPayload.Installation.class);

        assertThat(event.getAction(), is("deleted"));
        assertThat(event.getInstallation().getId(), is(2L));
        assertThat(event.getInstallation().getAccount().getLogin(), is("octocat"));
        assertThat(event.getInstallation().getAccount().getType(), is("User"));
        assertThat(event.getInstallation().getAccount(), instanceOf(GHUser.class));

        assertThat(event.getRepositories().get(0).getId(), is(1296269L));
        assertThat(event.getRepositories().get(0).getNodeId(), is("MDEwOlJlcG9zaXRvcnkxMjk2MjY5"));
        assertThat(event.getRepositories().get(0).getName(), is("Hello-World"));
        assertThat(event.getRepositories().get(0).getFullName(), is("octocat/Hello-World"));
        assertThat(event.getRepositories().get(0).isPrivate(), is(false));
        assertThat(event.getRepositories().get(0).getOwner().getLogin(), is("octocat"));

        assertThat(event.getSender().getLogin(), is("octocat"));
    }

    /**
     * Workflow dispatch.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Payload("installation_organization")
    public void InstallationInOrganizationEvent() throws IOException {
        final GHEventPayload.Installation event = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .build()
                .parseEventPayload(payload.asReader(), GHEventPayload.Installation.class);

        assertThat(event.getAction(), is("deleted"));
        assertThat(event.getInstallation().getId(), is(2L));
        assertThat(event.getInstallation().getAccount().getLogin(), is("github"));
        assertThat(event.getInstallation().getAccount().getType(), is("Organization"));
        assertThat(event.getInstallation().getAccount(), instanceOf(GHOrganization.class));

        assertThat(event.getRepositories().get(0).getId(), is(1296269L));
        assertThat(event.getRepositories().get(0).getNodeId(), is("MDEwOlJlcG9zaXRvcnkxMjk2MjY5"));
        assertThat(event.getRepositories().get(0).getName(), is("Hello-World"));
        assertThat(event.getRepositories().get(0).getFullName(), is("github/Hello-World"));
        assertThat(event.getRepositories().get(0).isPrivate(), is(false));
        assertThat(event.getRepositories().get(0).getOwner().getLogin(), is("github"));

        assertThat(event.getSender().getLogin(), is("octocat"));
    }

    @Test
    public void workflow_dispatch() throws Exception {
        final GHEventPayload.WorkflowDispatch workflowDispatchPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.WorkflowDispatch.class);

        assertThat(workflowDispatchPayload.getRef(), is("refs/heads/main"));
        assertThat(workflowDispatchPayload.getAction(), is(nullValue()));
        assertThat(workflowDispatchPayload.getWorkflow(), is(".github/workflows/main.yml"));
        assertThat(workflowDispatchPayload.getInputs(), aMapWithSize(1));
        assertThat(workflowDispatchPayload.getInputs().keySet(), contains("logLevel"));
        assertThat(workflowDispatchPayload.getInputs().values(), contains("warning"));
        assertThat(workflowDispatchPayload.getRepository().getName(), is("quarkus-bot-java-playground"));
        assertThat(workflowDispatchPayload.getSender().getLogin(), is("gsmet"));
    }

    /**
     * Workflow run.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void workflow_run() throws Exception {
        final GHEventPayload.WorkflowRun workflowRunPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.WorkflowRun.class);

        assertThat(workflowRunPayload.getAction(), is("completed"));
        assertThat(workflowRunPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(workflowRunPayload.getSender().getLogin(), is("gsmet"));

        GHWorkflow workflow = workflowRunPayload.getWorkflow();
        assertThat(workflow.getId(), is(7087581L));
        assertThat(workflow.getName(), is("CI"));
        assertThat(workflow.getPath(), is(".github/workflows/main.yml"));
        assertThat(workflow.getState(), is("active"));
        assertThat(workflow.getUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/workflows/7087581"));
        assertThat(workflow.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/blob/main/.github/workflows/main.yml"));
        assertThat(workflow.getBadgeUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/workflows/CI/badge.svg"));

        GHWorkflowRun workflowRun = workflowRunPayload.getWorkflowRun();
        assertThat(workflowRun.getId(), is(680604745L));
        assertThat(workflowRun.getName(), is("CI"));
        assertThat(workflowRun.getHeadBranch(), is("main"));
        assertThat(workflowRun.getHeadSha(), is("dbea8d8b6ed2cf764dfd84a215f3f9040b3d4423"));
        assertThat(workflowRun.getRunNumber(), is(6L));
        assertThat(workflowRun.getEvent(), is(GHEvent.WORKFLOW_DISPATCH));
        assertThat(workflowRun.getStatus(), is(GHWorkflowRun.Status.COMPLETED));
        assertThat(workflowRun.getConclusion(), is(GHWorkflowRun.Conclusion.SUCCESS));
        assertThat(workflowRun.getWorkflowId(), is(7087581L));
        assertThat(workflowRun.getUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745"));
        assertThat(workflowRun.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/actions/runs/680604745"));
        assertThat(workflowRun.getJobsUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745/jobs"));
        assertThat(workflowRun.getLogsUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745/logs"));
        assertThat(workflowRun.getCheckSuiteUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/check-suites/2327154397"));
        assertThat(workflowRun.getArtifactsUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745/artifacts"));
        assertThat(workflowRun.getCancelUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745/cancel"));
        assertThat(workflowRun.getRerunUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/runs/680604745/rerun"));
        assertThat(workflowRun.getWorkflowUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/workflows/7087581"));
        assertThat(workflowRun.getCreatedAt().getTime(), is(1616524526000L));
        assertThat(workflowRun.getUpdatedAt().getTime(), is(1616524543000L));
        assertThat(workflowRun.getRunAttempt(), is(1L));
        assertThat(workflowRun.getRunStartedAt().getTime(), is(1616524526000L));
        assertThat(workflowRun.getHeadCommit().getId(), is("dbea8d8b6ed2cf764dfd84a215f3f9040b3d4423"));
        assertThat(workflowRun.getHeadCommit().getTreeId(), is("b17089e6a2574ec1002566fe980923e62dce3026"));
        assertThat(workflowRun.getHeadCommit().getMessage(), is("Update main.yml"));
        assertThat(workflowRun.getHeadCommit().getTimestamp().getTime(), is(1616523390000L));
        assertThat(workflowRun.getHeadCommit().getAuthor().getName(), is("Guillaume Smet"));
        assertThat(workflowRun.getHeadCommit().getAuthor().getEmail(), is("guillaume.smet@gmail.com"));
        assertThat(workflowRun.getHeadCommit().getCommitter().getName(), is("GitHub"));
        assertThat(workflowRun.getHeadCommit().getCommitter().getEmail(), is("noreply@github.com"));
        assertThat(workflowRun.getHeadRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(workflowRun.getRepository(), sameInstance(workflowRunPayload.getRepository()));
    }

    /**
     * Workflow run pull request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void workflow_run_pull_request() throws Exception {
        final GHEventPayload.WorkflowRun workflowRunPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.WorkflowRun.class);

        List<GHPullRequest> pullRequests = workflowRunPayload.getWorkflowRun().getPullRequests();
        assertThat(pullRequests.size(), is(1));

        GHPullRequest pullRequest = pullRequests.get(0);
        assertThat(pullRequest.getId(), is(599098265L));
        assertThat(pullRequest.getRepository(), sameInstance(workflowRunPayload.getRepository()));
    }

    /**
     * Workflow run other repository.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void workflow_run_other_repository() throws Exception {
        final GHEventPayload.WorkflowRun workflowRunPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.WorkflowRun.class);
        GHWorkflowRun workflowRun = workflowRunPayload.getWorkflowRun();

        assertThat(workflowRunPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(workflowRun.getHeadRepository().getFullName(),
                is("gsmet-bot-playground/quarkus-bot-java-playground"));
        assertThat(workflowRun.getRepository(), sameInstance(workflowRunPayload.getRepository()));
        assertThat(workflowRunPayload.getWorkflow().getRepository(), sameInstance(workflowRunPayload.getRepository()));
    }

    /**
     * Workflow job.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void workflow_job() throws Exception {
        final GHEventPayload.WorkflowJob workflowJobPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.WorkflowJob.class);

        assertThat(workflowJobPayload.getAction(), is("completed"));
        assertThat(workflowJobPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(workflowJobPayload.getSender().getLogin(), is("gsmet"));

        GHWorkflowJob workflowJob = workflowJobPayload.getWorkflowJob();
        assertThat(workflowJob.getId(), is(6653410527L));
        assertThat(workflowJob.getRunId(), is(2408553341L));
        assertThat(workflowJob.getRunAttempt(), is(1));
        assertThat(workflowJob.getUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/actions/jobs/6653410527"));
        assertThat(workflowJob.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/runs/6653410527?check_suite_focus=true"));
        assertThat(workflowJob.getNodeId(), is("CR_kwDOEq3cwc8AAAABjJL83w"));
        assertThat(workflowJob.getHeadSha(), is("5dd2dadfbdc2a722c08a8ad42ae4e26e3e731042"));
        assertThat(workflowJob.getStatus(), is(GHWorkflowRun.Status.COMPLETED));
        assertThat(workflowJob.getConclusion(), is(GHWorkflowRun.Conclusion.FAILURE));
        assertThat(workflowJob.getStartedAt().getTime(), is(1653908125000L));
        assertThat(workflowJob.getCompletedAt().getTime(), is(1653908157000L));
        assertThat(workflowJob.getName(), is("JVM Tests - JDK JDK16"));
        assertThat(workflowJob.getSteps(),
                contains(hasProperty("name", is("Set up job")),
                        hasProperty("name", is("Run actions/checkout@v2")),
                        hasProperty("name", is("Build with Maven")),
                        hasProperty("name", is("Post Run actions/checkout@v2")),
                        hasProperty("name", is("Complete job"))));
        assertThat(workflowJob.getCheckRunUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/check-runs/6653410527"));
    }

    /**
     * Label created.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void label_created() throws Exception {
        final GHEventPayload.Label labelPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Label.class);
        GHLabel label = labelPayload.getLabel();

        assertThat(labelPayload.getAction(), is("created"));
        assertThat(labelPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(label.getId(), is(2901546662L));
        assertThat(label.getNodeId(), is("MDU6TGFiZWwyOTAxNTQ2NjYy"));
        assertThat(label.getName(), is("new-label"));
        assertThat(label.getColor(), is("f9d0c4"));
        assertThat(label.isDefault(), is(false));
        assertThat(label.getDescription(), is("description"));
    }

    /**
     * Label edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void label_edited() throws Exception {
        final GHEventPayload.Label labelPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Label.class);
        GHLabel label = labelPayload.getLabel();

        assertThat(labelPayload.getAction(), is("edited"));
        assertThat(labelPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(label.getId(), is(2901546662L));
        assertThat(label.getNodeId(), is("MDU6TGFiZWwyOTAxNTQ2NjYy"));
        assertThat(label.getName(), is("new-label-updated"));
        assertThat(label.getColor(), is("4AE686"));
        assertThat(label.isDefault(), is(false));
        assertThat(label.getDescription(), is("description"));

        assertThat(labelPayload.getChanges().getName().getFrom(), is("new-label"));
        assertThat(labelPayload.getChanges().getColor().getFrom(), is("f9d0c4"));
    }

    /**
     * Label deleted.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void label_deleted() throws Exception {
        GHEventPayload.Label labelPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Label.class);
        GHLabel label = labelPayload.getLabel();

        assertThat(labelPayload.getAction(), is("deleted"));
        assertThat(labelPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(label.getId(), is(2901546662L));
        assertThat(label.getNodeId(), is("MDU6TGFiZWwyOTAxNTQ2NjYy"));
        assertThat(label.getName(), is("new-label-updated"));
        assertThat(label.getColor(), is("4AE686"));
        assertThat(label.isDefault(), is(false));
        assertThat(label.getDescription(), is("description"));
    }

    /**
     * Discussion created.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void discussion_created() throws Exception {
        final GHEventPayload.Discussion discussionPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Discussion.class);

        assertThat(discussionPayload.getAction(), is("created"));
        assertThat(discussionPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(discussionPayload.getSender().getLogin(), is("gsmet"));

        GHRepositoryDiscussion discussion = discussionPayload.getDiscussion();

        GHRepositoryDiscussion.Category category = discussion.getCategory();

        assertThat(category.getId(), is(33522033L));
        assertThat(category.getNodeId(), is("DIC_kwDOEq3cwc4B_4Fx"));
        assertThat(category.getEmoji(), is(":pray:"));
        assertThat(category.getName(), is("Q&A"));
        assertThat(category.getDescription(), is("Ask the community for help"));
        assertThat(category.getCreatedAt().getTime(), is(1636991431000L));
        assertThat(category.getUpdatedAt().getTime(), is(1636991431000L));
        assertThat(category.getSlug(), is("q-a"));
        assertThat(category.isAnswerable(), is(true));

        assertThat(discussion.getAnswerHtmlUrl(), is(nullValue()));
        assertThat(discussion.getAnswerChosenAt(), is(nullValue()));
        assertThat(discussion.getAnswerChosenBy(), is(nullValue()));

        assertThat(discussion.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/discussions/78"));
        assertThat(discussion.getId(), is(3698909L));
        assertThat(discussion.getNodeId(), is("D_kwDOEq3cwc4AOHDd"));
        assertThat(discussion.getNumber(), is(78));
        assertThat(discussion.getTitle(), is("Title of discussion"));

        assertThat(discussion.getUser().getLogin(), is("gsmet"));
        assertThat(discussion.getUser().getId(), is(1279749L));
        assertThat(discussion.getUser().getNodeId(), is("MDQ6VXNlcjEyNzk3NDk="));

        assertThat(discussion.getState(), is(GHRepositoryDiscussion.State.OPEN));
        assertThat(discussion.isLocked(), is(false));
        assertThat(discussion.getComments(), is(0));
        assertThat(discussion.getCreatedAt().getTime(), is(1637584949000L));
        assertThat(discussion.getUpdatedAt().getTime(), is(1637584949000L));
        assertThat(discussion.getAuthorAssociation(), is(GHCommentAuthorAssociation.OWNER));
        assertThat(discussion.getActiveLockReason(), is(nullValue()));
        assertThat(discussion.getBody(), is("Body of discussion."));
    }

    /**
     * Discussion answered.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void discussion_answered() throws Exception {
        final GHEventPayload.Discussion discussionPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Discussion.class);

        assertThat(discussionPayload.getAction(), is("answered"));
        assertThat(discussionPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(discussionPayload.getSender().getLogin(), is("gsmet"));

        GHRepositoryDiscussion discussion = discussionPayload.getDiscussion();

        GHRepositoryDiscussion.Category category = discussion.getCategory();

        assertThat(category.getId(), is(33522033L));
        assertThat(category.getNodeId(), is("DIC_kwDOEq3cwc4B_4Fx"));
        assertThat(category.getEmoji(), is(":pray:"));
        assertThat(category.getName(), is("Q&A"));
        assertThat(category.getDescription(), is("Ask the community for help"));
        assertThat(category.getCreatedAt().getTime(), is(1636991431000L));
        assertThat(category.getUpdatedAt().getTime(), is(1636991431000L));
        assertThat(category.getSlug(), is("q-a"));
        assertThat(category.isAnswerable(), is(true));

        assertThat(discussion.getAnswerHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/discussions/78#discussioncomment-1681242"));
        assertThat(discussion.getAnswerChosenAt().getTime(), is(1637585047000L));
        assertThat(discussion.getAnswerChosenBy().getLogin(), is("gsmet"));

        assertThat(discussion.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/discussions/78"));
        assertThat(discussion.getId(), is(3698909L));
        assertThat(discussion.getNodeId(), is("D_kwDOEq3cwc4AOHDd"));
        assertThat(discussion.getNumber(), is(78));
        assertThat(discussion.getTitle(), is("Title of discussion"));

        assertThat(discussion.getUser().getLogin(), is("gsmet"));
        assertThat(discussion.getUser().getId(), is(1279749L));
        assertThat(discussion.getUser().getNodeId(), is("MDQ6VXNlcjEyNzk3NDk="));

        assertThat(discussion.getState(), is(GHRepositoryDiscussion.State.OPEN));
        assertThat(discussion.isLocked(), is(false));
        assertThat(discussion.getComments(), is(1));
        assertThat(discussion.getCreatedAt().getTime(), is(1637584949000L));
        assertThat(discussion.getUpdatedAt().getTime(), is(1637585047000L));
        assertThat(discussion.getAuthorAssociation(), is(GHCommentAuthorAssociation.OWNER));
        assertThat(discussion.getActiveLockReason(), is(nullValue()));
        assertThat(discussion.getBody(), is("Body of discussion."));
    }

    /**
     * Discussion labeled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void discussion_labeled() throws Exception {
        final GHEventPayload.Discussion discussionPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Discussion.class);

        assertThat(discussionPayload.getAction(), is("labeled"));
        assertThat(discussionPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(discussionPayload.getSender().getLogin(), is("gsmet"));

        GHRepositoryDiscussion discussion = discussionPayload.getDiscussion();

        GHRepositoryDiscussion.Category category = discussion.getCategory();

        assertThat(category.getId(), is(33522033L));
        assertThat(category.getNodeId(), is("DIC_kwDOEq3cwc4B_4Fx"));
        assertThat(category.getEmoji(), is(":pray:"));
        assertThat(category.getName(), is("Q&A"));
        assertThat(category.getDescription(), is("Ask the community for help"));
        assertThat(category.getCreatedAt().getTime(), is(1636991431000L));
        assertThat(category.getUpdatedAt().getTime(), is(1636991431000L));
        assertThat(category.getSlug(), is("q-a"));
        assertThat(category.isAnswerable(), is(true));

        assertThat(discussion.getAnswerHtmlUrl(), is(nullValue()));
        assertThat(discussion.getAnswerChosenAt(), is(nullValue()));
        assertThat(discussion.getAnswerChosenBy(), is(nullValue()));

        assertThat(discussion.getHtmlUrl().toString(),
                is("https://github.com/gsmet/quarkus-bot-java-playground/discussions/78"));
        assertThat(discussion.getId(), is(3698909L));
        assertThat(discussion.getNodeId(), is("D_kwDOEq3cwc4AOHDd"));
        assertThat(discussion.getNumber(), is(78));
        assertThat(discussion.getTitle(), is("Title of discussion"));

        assertThat(discussion.getUser().getLogin(), is("gsmet"));
        assertThat(discussion.getUser().getId(), is(1279749L));
        assertThat(discussion.getUser().getNodeId(), is("MDQ6VXNlcjEyNzk3NDk="));

        assertThat(discussion.getState(), is(GHRepositoryDiscussion.State.OPEN));
        assertThat(discussion.isLocked(), is(false));
        assertThat(discussion.getComments(), is(0));
        assertThat(discussion.getCreatedAt().getTime(), is(1637584949000L));
        assertThat(discussion.getUpdatedAt().getTime(), is(1637584961000L));
        assertThat(discussion.getAuthorAssociation(), is(GHCommentAuthorAssociation.OWNER));
        assertThat(discussion.getActiveLockReason(), is(nullValue()));
        assertThat(discussion.getBody(), is("Body of discussion."));

        GHLabel label = discussionPayload.getLabel();
        assertThat(label.getId(), is(2543373314L));
        assertThat(label.getNodeId(), is("MDU6TGFiZWwyNTQzMzczMzE0"));
        assertThat(label.getUrl().toString(),
                is("https://api.github.com/repos/gsmet/quarkus-bot-java-playground/labels/area/hibernate-validator"));
        assertThat(label.getName(), is("area/hibernate-validator"));
        assertThat(label.getColor(), is("ededed"));
        assertThat(label.isDefault(), is(false));
        assertThat(label.getDescription(), is(nullValue()));
    }

    /**
     * Starred.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void starred() throws Exception {
        final GHEventPayload.Star starPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Star.class);

        assertThat(starPayload.getAction(), is("created"));
        assertThat(starPayload.getRepository().getFullName(), is("gsmet/quarkus-bot-java-playground"));
        assertThat(starPayload.getSender().getLogin(), is("gsmet"));
        assertThat(starPayload.getStarredAt().getTime(), is(1654017876000L));
    }

    /**
     * Projectsv 2 item created.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void projectsv2item_created() throws Exception {
        final GHEventPayload.ProjectsV2Item projectsV2ItemPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.ProjectsV2Item.class);

        assertThat(projectsV2ItemPayload.getAction(), is("created"));

        assertThat(projectsV2ItemPayload.getProjectsV2Item().getId(), is(8083254L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getNodeId(), is("PVTI_lADOBNft-M4AEjBWzgB7VzY"));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getProjectNodeId(), is("PVT_kwDOBNft-M4AEjBW"));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getContentNodeId(), is("I_kwDOFOkjw85Ozz26"));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getContentType(), is(ContentType.ISSUE));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreator().getLogin(), is("gsmet"));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreator().getNodeId(), is("MDQ6VXNlcjEyNzk3NDk="));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreatedAt().getTime(), is(1659532028000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getUpdatedAt().getTime(), is(1659532028000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getArchivedAt(), is(nullValue()));

        assertThat(projectsV2ItemPayload.getOrganization().getLogin(), is("gsmet-bot-playground"));
        assertThat(projectsV2ItemPayload.getOrganization().getId(), is(81260024L));
        assertThat(projectsV2ItemPayload.getOrganization().getNodeId(), is("MDEyOk9yZ2FuaXphdGlvbjgxMjYwMDI0"));

        assertThat(projectsV2ItemPayload.getSender().getLogin(), is("gsmet"));
        assertThat(projectsV2ItemPayload.getSender().getId(), is(1279749L));
        assertThat(projectsV2ItemPayload.getSender().getNodeId(), is("MDQ6VXNlcjEyNzk3NDk="));

        assertThat(projectsV2ItemPayload.getInstallation().getId(), is(16779846L));
        assertThat(projectsV2ItemPayload.getInstallation().getNodeId(),
                is("MDIzOkludGVncmF0aW9uSW5zdGFsbGF0aW9uMTY3Nzk4NDY="));
    }

    /**
     * Projectsv 2 item edited.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void projectsv2item_edited() throws Exception {
        final GHEventPayload.ProjectsV2Item projectsV2ItemPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.ProjectsV2Item.class);

        assertThat(projectsV2ItemPayload.getAction(), is("edited"));

        assertThat(projectsV2ItemPayload.getProjectsV2Item().getId(), is(8083254L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreatedAt().getTime(), is(1659532028000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getUpdatedAt().getTime(), is(1659532033000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getArchivedAt(), is(nullValue()));

        assertThat(projectsV2ItemPayload.getChanges().getFieldValue().getFieldNodeId(),
                is("PVTF_lADOBNft-M4AEjBWzgCnp5Q"));
        assertThat(projectsV2ItemPayload.getChanges().getFieldValue().getFieldType(), is(FieldType.SINGLE_SELECT));
    }

    /**
     * Projectsv 2 item archived.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void projectsv2item_archived() throws Exception {
        final GHEventPayload.ProjectsV2Item projectsV2ItemPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.ProjectsV2Item.class);

        assertThat(projectsV2ItemPayload.getAction(), is("archived"));

        assertThat(projectsV2ItemPayload.getProjectsV2Item().getId(), is(8083794L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreatedAt().getTime(), is(1659532431000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getUpdatedAt().getTime(), is(1660086629000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getArchivedAt().getTime(), is(1660086629000L));

        assertThat(projectsV2ItemPayload.getChanges().getArchivedAt().getFrom(), is(nullValue()));
        assertThat(projectsV2ItemPayload.getChanges().getArchivedAt().getTo().getTime(), is(1660086629000L));
    }

    /**
     * Projectsv 2 item restored.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void projectsv2item_restored() throws Exception {
        final GHEventPayload.ProjectsV2Item projectsV2ItemPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.ProjectsV2Item.class);

        assertThat(projectsV2ItemPayload.getAction(), is("restored"));

        assertThat(projectsV2ItemPayload.getProjectsV2Item().getId(), is(8083254L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreatedAt().getTime(), is(1659532028000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getUpdatedAt().getTime(), is(1659532419000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getArchivedAt(), is(nullValue()));

        assertThat(projectsV2ItemPayload.getChanges().getArchivedAt().getFrom().getTime(), is(1659532142000L));
        assertThat(projectsV2ItemPayload.getChanges().getArchivedAt().getTo(), is(nullValue()));
    }

    /**
     * Projectsv 2 item reordered.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void projectsv2item_reordered() throws Exception {
        final GHEventPayload.ProjectsV2Item projectsV2ItemPayload = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.ProjectsV2Item.class);

        assertThat(projectsV2ItemPayload.getAction(), is("reordered"));

        assertThat(projectsV2ItemPayload.getProjectsV2Item().getId(), is(8083794L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getCreatedAt().getTime(), is(1659532431000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getUpdatedAt().getTime(), is(1659532439000L));
        assertThat(projectsV2ItemPayload.getProjectsV2Item().getArchivedAt(), is(nullValue()));

        assertThat(projectsV2ItemPayload.getChanges().getPreviousProjectsV2ItemNodeId().getFrom(),
                is("PVTI_lADOBNft-M4AEjBWzgB7VzY"));
        assertThat(projectsV2ItemPayload.getChanges().getPreviousProjectsV2ItemNodeId().getTo(),
                is("PVTI_lADOBNft-M4AEjBWzgB7VzY"));
    }
}

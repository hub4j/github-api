package org.kohsuke.github;

import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

import static org.hamcrest.Matchers.*;

public class GHEventPayloadTest extends AbstractGitHubWireMockTest {

    @Rule
    public final PayloadRule payload = new PayloadRule(".json");

    public GHEventPayloadTest() {
        useDefaultGitHub = false;
    }

    @Test
    public void commit_comment() throws Exception {
        GHEventPayload.CommitComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.CommitComment.class);
        assertThat(event.getAction(), is("created"));
        assertThat(event.getComment().getSHA1(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getComment().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void create() throws Exception {
        GHEventPayload.Create event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Create.class);
        assertThat(event.getRef(), is("0.0.1"));
        assertThat(event.getRefType(), is("tag"));
        assertThat(event.getMasterBranch(), is("master"));
        assertThat(event.getDescription(), is(""));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void delete() throws Exception {
        GHEventPayload.Delete event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Delete.class);
        assertThat(event.getRef(), is("simple-tag"));
        assertThat(event.getRefType(), is("tag"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void deployment() throws Exception {
        GHEventPayload.Deployment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Deployment.class);
        assertThat(event.getDeployment().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getDeployment().getEnvironment(), is("production"));
        assertThat(event.getDeployment().getCreator().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void deployment_status() throws Exception {
        GHEventPayload.DeploymentStatus event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.DeploymentStatus.class);
        assertThat(event.getDeploymentStatus().getState(), is(GHDeploymentState.SUCCESS));
        assertThat(event.getDeploymentStatus().getTargetUrl(), nullValue());
        assertThat(event.getDeployment().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getDeployment().getEnvironment(), is("production"));
        assertThat(event.getDeployment().getCreator().getLogin(), is("baxterthehacker"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void fork() throws Exception {
        GHEventPayload.Fork event = GitHub.offline().parseEventPayload(payload.asReader(), GHEventPayload.Fork.class);
        assertThat(event.getForkee().getName(), is("public-repo"));
        assertThat(event.getForkee().getOwner().getLogin(), is("baxterandthehackers"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterandthehackers"));
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

    @Test
    public void issue_comment() throws Exception {
        GHEventPayload.IssueComment event = GitHub.offline()
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
    }

    @Test
    public void issues() throws Exception {
        GHEventPayload.Issue event = GitHub.offline().parseEventPayload(payload.asReader(), GHEventPayload.Issue.class);
        assertThat(event.getAction(), is("opened"));
        assertThat(event.getIssue().getNumber(), is(2));
        assertThat(event.getIssue().getTitle(), is("Spelling error in the README file"));
        assertThat(event.getIssue().getState(), is(GHIssueState.OPEN));
        assertThat(event.getIssue().getLabels().size(), is(1));
        assertThat(event.getIssue().getLabels().iterator().next().getName(), is("bug"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
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

    @Test
    @Payload("public")
    public void public_() throws Exception {
        GHEventPayload.Public event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Public.class);
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void pull_request() throws Exception {
        GHEventPayload.PullRequest event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequest.class);
        assertThat(event.getAction(), is("opened"));
        assertThat(event.getNumber(), is(1));
        assertThat(event.getPullRequest().getNumber(), is(1));
        assertThat(event.getPullRequest().getTitle(), is("Update the README with new information"));
        assertThat(event.getPullRequest().getBody(),
                is("This is a pretty simple change that we need to pull into " + "master."));
        assertThat(event.getPullRequest().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getRef(), is("changes"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("baxterthehacker:changes"));
        assertThat(event.getPullRequest().getHead().getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("master"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:master"));
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
    }

    @Test
    public void pull_request_review() throws Exception {
        GHEventPayload.PullRequestReview event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequestReview.class);
        assertThat(event.getAction(), is("submitted"));

        assertThat(event.getReview().getId(), is(2626884L));
        assertThat(event.getReview().getBody(), is("Looks great!\n"));
        assertThat(event.getReview().getState(), is(GHPullRequestReviewState.APPROVED));

        assertThat(event.getPullRequest().getNumber(), is(8));
        assertThat(event.getPullRequest().getTitle(), is("Add a README description"));
        assertThat(event.getPullRequest().getBody(), is("Just a few more details"));
        assertThat(event.getPullRequest().getUser().getLogin(), is("skalnik"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("skalnik"));
        assertThat(event.getPullRequest().getHead().getRef(), is("patch-2"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("skalnik:patch-2"));
        assertThat(event.getPullRequest().getHead().getSha(), is("b7a1f9c27caa4e03c14a88feb56e2d4f7500aa63"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("master"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:master"));
        assertThat(event.getPullRequest().getBase().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));

        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));

        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void pull_request_review_comment() throws Exception {
        GHEventPayload.PullRequestReviewComment event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.PullRequestReviewComment.class);
        assertThat(event.getAction(), is("created"));

        assertThat(event.getComment().getBody(), is("Maybe you should use more emojji on this line."));

        assertThat(event.getPullRequest().getNumber(), is(1));
        assertThat(event.getPullRequest().getTitle(), is("Update the README with new information"));
        assertThat(event.getPullRequest().getBody(),
                is("This is a pretty simple change that we need to pull into master."));
        assertThat(event.getPullRequest().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getHead().getRef(), is("changes"));
        assertThat(event.getPullRequest().getHead().getLabel(), is("baxterthehacker:changes"));
        assertThat(event.getPullRequest().getHead().getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getPullRequest().getBase().getUser().getLogin(), is("baxterthehacker"));
        assertThat(event.getPullRequest().getBase().getRef(), is("master"));
        assertThat(event.getPullRequest().getBase().getLabel(), is("baxterthehacker:master"));
        assertThat(event.getPullRequest().getBase().getSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));

        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));

        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void push() throws Exception {
        GHEventPayload.Push event = GitHub.offline().parseEventPayload(payload.asReader(), GHEventPayload.Push.class);
        assertThat(event.getRef(), is("refs/heads/changes"));
        assertThat(event.getBefore(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getHead(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.isCreated(), is(false));
        assertThat(event.isDeleted(), is(false));
        assertThat(event.isForced(), is(false));
        assertThat(event.getCommits().size(), is(1));
        assertThat(event.getCommits().get(0).getSha(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
        assertThat(event.getCommits().get(0).getAuthor().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getCommits().get(0).getCommitter().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getCommits().get(0).getAdded().size(), is(0));
        assertThat(event.getCommits().get(0).getRemoved().size(), is(0));
        assertThat(event.getCommits().get(0).getModified().size(), is(1));
        assertThat(event.getCommits().get(0).getModified().get(0), is("README.md"));
        assertThat(event.getRepository().getName(), is("public-repo"));
        assertThat(event.getRepository().getOwnerName(), is("baxterthehacker"));
        assertThat(event.getRepository().getUrl().toExternalForm(),
                is("https://github.com/baxterthehacker/public-repo"));
        assertThat(event.getPusher().getName(), is("baxterthehacker"));
        assertThat(event.getPusher().getEmail(), is("baxterthehacker@users.noreply.github.com"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    @Payload("push.fork")
    public void pushToFork() throws Exception {
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        GHEventPayload.Push event = GitHub.offline().parseEventPayload(payload.asReader(), GHEventPayload.Push.class);
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
        event = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub), GHEventPayload.Push.class);
        assertThat(event.getRepository().getUrl().toString(), is("https://github.com/hub4j-test-org/github-api"));
        assertThat(event.getRepository().getHttpTransportUrl(), is("https://github.com/hub4j-test-org/github-api.git"));

        event.getRepository().populate();

        // After populate the url is fixed to point to the correct API endpoint
        assertThat(event.getRepository().getUrl().toString(),
                is(mockGitHub.apiServer().baseUrl() + "/repos/hub4j-test-org/github-api"));
        assertThat(event.getRepository().getHttpTransportUrl(), is("https://github.com/hub4j-test-org/github-api.git"));

        // Source
        event = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub), GHEventPayload.Push.class);
        assertThat(event.getRepository().getSource().getFullName(), is("hub4j/github-api"));

        // Parent
        event = gitHub.parseEventPayload(payload.asReader(mockGitHub::mapToMockGitHub), GHEventPayload.Push.class);
        assertThat(event.getRepository().getParent().getFullName(), is("hub4j/github-api"));
    }

    // TODO implement support classes and write test
    // @Test
    // public void release() throws Exception {}

    @Test
    public void repository() throws Exception {
        GHEventPayload.Repository event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Repository.class);
        assertThat(event.getAction(), is("created"));
        assertThat(event.getRepository().getName(), is("new-repository"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterandthehackers"));
        assertThat(event.getOrganization().getLogin(), is("baxterandthehackers"));
        assertThat(event.getSender().getLogin(), is("baxterthehacker"));
    }

    @Test
    public void status() throws Exception {
        GHEventPayload.Status event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Status.class);
        assertThat(event.getContext(), is("default"));
        assertThat(event.getDescription(), is("status description"));
        assertThat(event.getState(), is(GHCommitState.SUCCESS));
        assertThat(event.getCommit().getSHA1(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
        assertThat(event.getRepository().getOwner().getLogin(), is("baxterthehacker"));
    }

    // TODO implement support classes and write test
    // @Test
    // public void team_add() throws Exception {}

    // TODO implement support classes and write test
    // @Test
    // public void watch() throws Exception {}

    @Test
    @Payload("check-run")
    public void checkRunEvent() throws Exception {
        GHEventPayload.CheckRun event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.CheckRun.class);
        assertThat(event.getRepository().getName(), is("Hello-World"));
        assertThat(event.getRepository().getOwner().getLogin(), is("Codertocat"));
        assertThat(event.getAction(), is("created"));

        // Checks the deserialization of check_run
        GHCheckRun checkRun = event.getCheckRun();
        assertThat(checkRun.getName(), is("Octocoders-linter"));
        assertThat(checkRun.getHeadSha(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkRun.getStatus(), is("completed"));
        assertThat(checkRun.getNodeId(), is("MDg6Q2hlY2tSdW4xMjg2MjAyMjg="));
        assertThat(checkRun.getExternalId(), is(""));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(checkRun.getStartedAt()), is("2019-05-15T15:21:12Z"));
        assertThat(formatter.format(checkRun.getCompletedAt()), is("2019-05-15T20:22:22Z"));

        assertThat(checkRun.getConclusion(), is("success"));
        assertThat(checkRun.getUrl().toString(),
                is("https://api.github.com/repos/Codertocat/Hello-World/check-runs/128620228"));
        assertThat(checkRun.getHtmlUrl().toString(), is("https://github.com/Codertocat/Hello-World/runs/128620228"));
        assertThat(checkRun.getDetailsUrl().toString(), is("https://octocoders.io"));
        assertThat(checkRun.getApp().getId(), is(29310L));
        assertThat(checkRun.getCheckSuite().getId(), is(118578147L));
        assertThat(checkRun.getOutput().getTitle(), is("check-run output"));
        assertThat(checkRun.getOutput().getSummary(), nullValue());
        assertThat(checkRun.getOutput().getText(), nullValue());
        assertThat(checkRun.getOutput().getAnnotationsCount(), is(0));
        assertThat(checkRun.getOutput().getAnnotationsUrl().toString(),
                is("https://api.github.com/repos/Codertocat/Hello-World/check-runs/128620228/annotations"));

        // Checks the deserialization of sender
        assertThat(event.getSender().getId(), is(21031067L));
    }

    @Test
    @Payload("check-suite")
    public void checkSuiteEvent() throws Exception {
        GHEventPayload.CheckSuite event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.CheckSuite.class);

        assertThat(event.getRepository().getName(), is("Hello-World"));
        assertThat(event.getRepository().getOwner().getLogin(), is("Codertocat"));
        assertThat(event.getAction(), is("completed"));
        assertThat(event.getSender().getId(), is(21031067L));

        // Checks the deserialization of check_suite
        GHCheckSuite checkSuite = event.getCheckSuite();
        assertThat(checkSuite.getNodeId(), is("MDEwOkNoZWNrU3VpdGUxMTg1NzgxNDc="));
        assertThat(checkSuite.getHeadBranch(), is("changes"));
        assertThat(checkSuite.getHeadSha(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getStatus(), is("completed"));
        assertThat(checkSuite.getConclusion(), is("success"));
        assertThat(checkSuite.getBefore(), is("6113728f27ae82c7b1a177c8d03f9e96e0adf246"));
        assertThat(checkSuite.getAfter(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getLatestCheckRunsCount(), is(1));
        assertThat(checkSuite.getCheckRunsUrl().toString(),
                is("https://api.github.com/repos/Codertocat/Hello-World/check-suites/118578147/check-runs"));
        assertThat(checkSuite.getHeadCommit().getMessage(), is("Update README.md"));
        assertThat(checkSuite.getHeadCommit().getId(), is("ec26c3e57ca3a959ca5aad62de7213c562f8c821"));
        assertThat(checkSuite.getHeadCommit().getTreeId(), is("31b122c26a97cf9af023e9ddab94a82c6e77b0ea"));
        assertThat(checkSuite.getHeadCommit().getAuthor().getName(), is("Codertocat"));
        assertThat(checkSuite.getHeadCommit().getCommitter().getName(), is("Codertocat"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(checkSuite.getHeadCommit().getTimestamp()), is("2019-05-15T15:20:30Z"));

        assertThat(checkSuite.getApp().getId(), is(29310L));
    }

    @Test
    @Payload("installation_repositories")
    public void InstallationRepositoriesEvent() throws Exception {
        GHEventPayload.InstallationRepositories event = GitHub.offline()
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

    @Test
    @Payload("installation")
    public void InstallationEvent() throws Exception {
        GHEventPayload.Installation event = GitHub.offline()
                .parseEventPayload(payload.asReader(), GHEventPayload.Installation.class);

        assertThat(event.getAction(), is("deleted"));
        assertThat(event.getInstallation().getId(), is(2L));
        assertThat(event.getInstallation().getAccount().getLogin(), is("octocat"));

        assertThat(event.getRepositories().get(0).getId(), is(1296269L));
        assertThat(event.getRepositories().get(0).getNodeId(), is("MDEwOlJlcG9zaXRvcnkxODY4NTMwMDc="));
        assertThat(event.getRepositories().get(0).getName(), is("Hello-World"));
        assertThat(event.getRepositories().get(0).getFullName(), is("octocat/Hello-World"));
        assertThat(event.getRepositories().get(0).isPrivate(), is(false));

        assertThat(event.getSender().getLogin(), is("octocat"));
    }
}

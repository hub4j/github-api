package org.kohsuke.github;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

/**
 * Unit test for simple App.
 */
public class AppTest extends AbstractGitHubWireMockTest {
    static final String GITHUB_API_TEST_REPO = "github-api-test";

    @Test
    public void testRepoCRUD() throws Exception {
        String targetName = "github-api-test-rename2";

        cleanupUserRepository("github-api-test-rename");
        cleanupUserRepository(targetName);

        GHRepository r = gitHub.createRepository("github-api-test-rename")
                .description("a test repository")
                .homepage("http://github-api.kohsuke.org/")
                .private_(false)
                .create();

        assertThat(r.hasIssues(), is(true));
        assertThat(r.hasWiki(), is(true));
        assertThat(r.hasDownloads(), is(true));
        assertThat(r.hasProjects(), is(true));

        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        r.enableProjects(false);

        r.renameTo(targetName);

        // local instance remains unchanged
        assertThat(r.getName(), equalTo("github-api-test-rename"));
        assertThat(r.hasIssues(), is(true));
        assertThat(r.hasWiki(), is(true));
        assertThat(r.hasDownloads(), is(true));
        assertThat(r.hasProjects(), is(true));

        r = gitHub.getMyself().getRepository(targetName);

        // values are updated
        assertThat(r.hasIssues(), is(false));
        assertThat(r.hasWiki(), is(false));
        assertThat(r.hasDownloads(), is(false));
        assertThat(r.getName(), equalTo(targetName));

        assertThat(r.hasProjects(), is(false));

        r.delete();
    }

    @Test
    public void testRepositoryWithAutoInitializationCRUD() throws Exception {
        String name = "github-api-test-autoinit";
        cleanupUserRepository(name);
        GHRepository r = gitHub.createRepository(name)
                .description("a test repository for auto init")
                .homepage("http://github-api.kohsuke.org/")
                .autoInit(true)
                .create();
        if (mockGitHub.isUseProxy()) {
            Thread.sleep(3000);
        }
        assertThat(r.getReadme(), notNullValue());

        r.delete();
    }

    private void cleanupUserRepository(final String name) throws IOException {
        if (mockGitHub.isUseProxy()) {
            cleanupRepository(getUser(getNonRecordingGitHub()).getLogin() + "/" + name);
        }
    }

    @Test
    public void testCredentialValid() throws IOException {
        assertThat(gitHub.isCredentialValid(), is(true));
        assertThat(gitHub.lastRateLimit().getCore(), not(instanceOf(GHRateLimit.UnknownLimitRecord.class)));
        assertThat(gitHub.lastRateLimit().getCore().getLimit(), equalTo(5000));

        gitHub = getGitHubBuilder().withOAuthToken("bogus", "user")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertThat(gitHub.isCredentialValid(), is(false));
        // For invalid credentials, we get a 401 but it includes anonymous rate limit headers
        assertThat(gitHub.lastRateLimit().getCore(), not(instanceOf(GHRateLimit.UnknownLimitRecord.class)));
        assertThat(gitHub.lastRateLimit().getCore().getLimit(), equalTo(60));
    }

    @Test
    public void testCredentialValidEnterprise() throws IOException {
        // Simulated GHE: getRateLimit returns 404
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertThat(gitHub.lastRateLimit().getCore().isExpired(), is(true));
        assertThat(gitHub.isCredentialValid(), is(true));

        // lastRateLimitUpdates because 404 still includes header rate limit info
        assertThat(gitHub.lastRateLimit(), notNullValue());
        assertThat(gitHub.lastRateLimit(), not(equalTo(GHRateLimit.DEFAULT)));
        assertThat(gitHub.lastRateLimit().getCore().isExpired(), is(false));

        gitHub = getGitHubBuilder().withOAuthToken("bogus", "user")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertThat(gitHub.isCredentialValid(), is(false));
        // Simulated GHE: For invalid credentials, we get a 401 that does not include ratelimit info
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
    }

    @Test
    public void testIssueWithNoComment() throws IOException {
        GHRepository repository = gitHub.getRepository("kohsuke/test");
        GHIssue i = repository.getIssue(4);
        List<GHIssueComment> v = i.getComments();
        // System.out.println(v);
        assertThat(v, is(empty()));
    }

    @Test
    public void testIssueWithComment() throws IOException {
        GHRepository repository = gitHub.getRepository("kohsuke/test");
        GHIssue i = repository.getIssue(3);
        List<GHIssueComment> v = i.getComments();
        // System.out.println(v);
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(0).getHtmlUrl().toString(),
                equalTo("https://github.com/kohsuke/test/issues/3#issuecomment-8547249"));
        assertThat(v.get(0).getUrl().toString(), endsWith("/repos/kohsuke/test/issues/comments/8547249"));
        assertThat(v.get(0).getNodeId(), equalTo("MDEyOklzc3VlQ29tbWVudDg1NDcyNDk="));
        assertThat(v.get(0).getParent().getNumber(), equalTo(3));
        assertThat(v.get(0).getParent().getId(), equalTo(6863845L));
        assertThat(v.get(0).getUser().getLogin(), equalTo("kohsuke"));
        assertThat(v.get(0).listReactions().toList(), is(empty()));

        assertThat(v.get(1).getHtmlUrl().toString(),
                equalTo("https://github.com/kohsuke/test/issues/3#issuecomment-8547251"));
        assertThat(v.get(1).getUrl().toString(), endsWith("/repos/kohsuke/test/issues/comments/8547251"));
        assertThat(v.get(1).getNodeId(), equalTo("MDEyOklzc3VlQ29tbWVudDg1NDcyNTE="));
        assertThat(v.get(1).getParent().getNumber(), equalTo(3));
        assertThat(v.get(1).getUser().getLogin(), equalTo("kohsuke"));
        List<GHReaction> reactions = v.get(1).listReactions().toList();
        assertThat(reactions.size(), equalTo(3));
        assertThat(reactions.stream().map(item -> item.getContent()).collect(Collectors.toList()),
                containsInAnyOrder(ReactionContent.EYES, ReactionContent.HOORAY, ReactionContent.ROCKET));

        // TODO: Add comment CRUD test

        GHReaction reaction = null;
        try {
            reaction = v.get(1).createReaction(ReactionContent.CONFUSED);
            v = i.getComments();
            reactions = v.get(1).listReactions().toList();
            assertThat(reactions.stream().map(item -> item.getContent()).collect(Collectors.toList()),
                    containsInAnyOrder(ReactionContent.CONFUSED,
                            ReactionContent.EYES,
                            ReactionContent.HOORAY,
                            ReactionContent.ROCKET));

            // test retired delete reaction API throws UnsupportedOperationException
            final GHReaction reactionToDelete = reaction;
            assertThrows(UnsupportedOperationException.class, () -> reactionToDelete.delete());

            // test new delete reaction API
            v.get(1).deleteReaction(reaction);
            reaction = null;
            v = i.getComments();
            reactions = v.get(1).listReactions().toList();
            assertThat(reactions.stream().map(item -> item.getContent()).collect(Collectors.toList()),
                    containsInAnyOrder(ReactionContent.EYES, ReactionContent.HOORAY, ReactionContent.ROCKET));
        } finally {
            if (reaction != null) {
                v.get(1).deleteReaction(reaction);
                reaction = null;
            }
        }
    }

    @Test
    public void testCreateIssue() throws IOException {
        GHUser u = getUser();
        GHRepository repository = getTestRepository();
        GHMilestone milestone = repository.createMilestone("Test Milestone Title3", "Test Milestone");
        GHIssue o = repository.createIssue("testing")
                .body("this is body")
                .assignee(u)
                .label("bug")
                .label("question")
                .milestone(milestone)
                .create();
        assertThat(o, notNullValue());
        assertThat(o.getBody(), equalTo("this is body"));

        // test locking
        assertThat(o.isLocked(), is(false));
        o.lock();
        o = repository.getIssue(o.getNumber());
        assertThat(o.isLocked(), is(true));
        o.unlock();
        o = repository.getIssue(o.getNumber());
        assertThat(o.isLocked(), is(false));

        o.close();
    }

    @Test
    public void testCreateAndListDeployments() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("main")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .description("question")
                .environment("unittest")
                .create();
        try {
            assertThat(deployment.getCreator(), notNullValue());
            assertThat(deployment.getId(), notNullValue());
            List<GHDeployment> deployments = repository.listDeployments(null, "main", null, "unittest").toList();
            assertThat(deployments, notNullValue());
            assertThat(deployments, is(not(emptyIterable())));
            GHDeployment unitTestDeployment = deployments.get(0);
            assertThat(unitTestDeployment.getEnvironment(), equalTo("unittest"));
            assertThat(unitTestDeployment.getOriginalEnvironment(), equalTo("unittest"));
            assertThat(unitTestDeployment.isProductionEnvironment(), equalTo(false));
            assertThat(unitTestDeployment.isTransientEnvironment(), equalTo(false));
            assertThat(unitTestDeployment.getRef(), equalTo("main"));
        } finally {
            // deployment.delete();
            assert true;
        }
    }

    @Test
    public void testGetDeploymentStatuses() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("main")
                .description("question")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .create();
        try {
            GHDeploymentStatus ghDeploymentStatus = deployment.createStatus(GHDeploymentState.QUEUED)
                    .description("success")
                    .targetUrl("http://www.github.com")
                    .logUrl("http://www.github.com/logurl")
                    .environmentUrl("http://www.github.com/envurl")
                    .environment("new-ci-env")
                    .create();
            Iterable<GHDeploymentStatus> deploymentStatuses = deployment.listStatuses();
            assertThat(deploymentStatuses, notNullValue());
            assertThat(Iterables.size(deploymentStatuses), equalTo(1));
            GHDeploymentStatus actualStatus = Iterables.get(deploymentStatuses, 0);
            assertThat(actualStatus.getId(), equalTo(ghDeploymentStatus.getId()));
            assertThat(actualStatus.getState(), equalTo(ghDeploymentStatus.getState()));
            assertThat(actualStatus.getLogUrl(), equalTo(ghDeploymentStatus.getLogUrl()));
            // Target url was deprecated and replaced with log url. The gh api will
            // prefer the log url value and return it in place of target url.
            assertThat(actualStatus.getLogUrl(), equalTo(ghDeploymentStatus.getTargetUrl()));
            assertThat(ghDeploymentStatus.getDeploymentUrl(), equalTo(deployment.getUrl()));
            assertThat(ghDeploymentStatus.getRepositoryUrl(), equalTo(repository.getUrl()));
        } finally {
            // deployment.delete();
            assert true;
        }
    }

    @Test
    public void testGetIssues() throws Exception {
        List<GHIssue> closedIssues = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .getIssues(GHIssueState.CLOSED);
        // prior to using PagedIterable GHRepository.getIssues(GHIssueState) would only retrieve 30 issues
        assertThat(closedIssues.size(), greaterThan(150));
        String readRepoString = GitHub.getMappingObjectWriter().writeValueAsString(closedIssues.get(0));
    }

    @Test
    public void testQueryIssues() throws IOException {
        final GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("testQueryIssues");
        List<GHIssue> openBugIssues = repo.queryIssues()
                .milestone("1")
                .creator(gitHub.getMyself().getLogin())
                .state(GHIssueState.OPEN)
                .label("bug")
                .pageSize(10)
                .list()
                .toList();
        GHIssue issueWithMilestone = openBugIssues.get(0);
        assertThat(openBugIssues, is(not(empty())));
        assertThat(openBugIssues, hasSize(1));
        assertThat(issueWithMilestone.getTitle(), is("Issue with milestone"));
        assertThat(issueWithMilestone.getAssignee().getLogin(), is("bloslo"));
        assertThat(issueWithMilestone.getBody(), containsString("@bloslo"));

        List<GHIssue> openIssuesWithAssignee = repo.queryIssues()
                .assignee(gitHub.getMyself().getLogin())
                .state(GHIssueState.OPEN)
                .list()
                .toList();
        GHIssue issueWithAssignee = openIssuesWithAssignee.get(0);
        assertThat(openIssuesWithAssignee, is(not(empty())));
        assertThat(openIssuesWithAssignee, hasSize(1));
        assertThat(issueWithAssignee.getLabels(), hasSize(2));
        assertThat(issueWithAssignee.getMilestone(), is(notNullValue()));

        List<GHIssue> allIssuesSince = repo.queryIssues()
                .mentioned(gitHub.getMyself().getLogin())
                .state(GHIssueState.ALL)
                .since(1632411646L)
                .sort(GHIssueQueryBuilder.Sort.COMMENTS)
                .direction(GHDirection.ASC)
                .list()
                .toList();
        GHIssue issueSince = allIssuesSince.get(3);
        assertThat(allIssuesSince, is(not(empty())));
        assertThat(allIssuesSince, hasSize(4));
        assertThat(issueSince.getBody(), is("Test closed issue @bloslo"));
        assertThat(issueSince.getState(), is(GHIssueState.CLOSED));

        List<GHIssue> allIssuesWithLabels = repo.queryIssues()
                .label("bug")
                .label("test-label")
                .state(GHIssueState.ALL)
                .list()
                .toList();
        GHIssue issueWithLabel = allIssuesWithLabels.get(0);
        assertThat(allIssuesWithLabels, is(not(empty())));
        assertThat(allIssuesWithLabels, hasSize(5));
        assertThat(issueWithLabel.getComments(), hasSize(2));
        assertThat(issueWithLabel.getTitle(), is("Issue with comments"));

        List<GHIssue> issuesWithLabelNull = repo.queryIssues().label(null).list().toList();
        GHIssue issueWithLabelNull = issuesWithLabelNull.get(2);
        assertThat(issuesWithLabelNull, is(not(empty())));
        assertThat(issuesWithLabelNull, hasSize(6));
        assertThat(issueWithLabelNull.getTitle(), is("Closed issue"));
        assertThat(issueWithLabelNull.getBody(), is("Test closed issue @bloslo"));
        assertThat(issueWithLabelNull.getState(), is(GHIssueState.OPEN));

        List<GHIssue> issuesWithLabelEmptyString = repo.queryIssues().label("").state(GHIssueState.ALL).list().toList();
        GHIssue issueWithLabelEmptyString = issuesWithLabelEmptyString.get(0);
        assertThat(issuesWithLabelEmptyString, is(not(empty())));
        assertThat(issuesWithLabelEmptyString, hasSize(8));
        assertThat(issueWithLabelEmptyString.getTitle(), is("Closed issue"));
        assertThat(issueWithLabelEmptyString.getBody(), is("Test closed issue @bloslo"));
    }

    private GHRepository getTestRepository() throws IOException {
        return getTempRepository(GITHUB_API_TEST_REPO);
    }

    @Test
    public void testListIssues() throws IOException {
        Iterable<GHIssue> closedIssues = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .listIssues(GHIssueState.CLOSED);

        int x = 0;
        for (GHIssue issue : closedIssues) {
            assertThat(issue, notNullValue());
            x++;
        }

        assertThat(x, greaterThan(150));
    }

    @Test
    public void testRateLimit() throws IOException {
        assertThat(gitHub.getRateLimit(), notNullValue());
    }

    @Test
    public void testMyOrganizations() throws IOException {
        Map<String, GHOrganization> org = gitHub.getMyOrganizations();
        assertThat(org.containsKey(null), is(false));
        // System.out.println(org);
    }

    @Test
    public void testMyOrganizationsContainMyTeams() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        Map<String, GHOrganization> myOrganizations = gitHub.getMyOrganizations();
        // GitHub no longer has default 'owners' team, so there may be organization memberships without a team
        // https://help.github.com/articles/about-improved-organization-permissions/
        assertThat(myOrganizations.keySet().containsAll(teams.keySet()), is(true));
    }

    @Test
    public void testMyTeamsShouldIncludeMyself() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        for (Entry<String, Set<GHTeam>> teamsPerOrg : teams.entrySet()) {
            String organizationName = teamsPerOrg.getKey();
            for (GHTeam team : teamsPerOrg.getValue()) {
                String teamName = team.getName();
                assertThat("Team " + teamName + " in organization " + organizationName + " does not contain myself",
                        shouldBelongToTeam(organizationName, teamName));
            }
        }
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreSome() throws IOException {
        // kohsuke had some public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "kohsuke";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations(user);
        assertThat(orgs.size(), greaterThan(0));
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreNone() throws IOException {
        // bitwiseman had no public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "bitwiseman";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations(user);
        assertThat(orgs.size(), equalTo(0));
    }

    private boolean shouldBelongToTeam(String organizationName, String teamName) throws IOException {
        GHOrganization org = gitHub.getOrganization(organizationName);
        assertThat(org, notNullValue());
        GHTeam team = org.getTeamByName(teamName);
        assertThat(team, notNullValue());
        return team.hasMember(gitHub.getMyself());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFetchingTeamFromGitHubInstanceThrowsException() throws Exception {
        GHOrganization organization = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamByName = organization.getTeams().get("Core Developers");

        assertThrows(UnsupportedOperationException.class, () -> gitHub.getTeam((int) teamByName.getId()));
    }

    @Test
    public void testShouldFetchTeamFromOrganization() throws Exception {
        GHOrganization organization = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamByName = organization.getTeams().get("Core Developers");

        GHTeam teamById = organization.getTeam(teamByName.getId());
        assertThat(teamById, notNullValue());

        assertThat(teamById.getId(), equalTo(teamByName.getId()));
        assertThat(teamById.getDescription(), equalTo(teamByName.getDescription()));

        GHTeam teamById2 = organization.getTeam(teamByName.getId());
        assertThat(teamById2, notNullValue());

        assertThat(teamById2.getId(), equalTo(teamByName.getId()));
        assertThat(teamById2.getDescription(), equalTo(teamByName.getDescription()));

    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequest() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertThat(r.getMasterBranch(), equalTo("main"));
        assertThat(r.getDefaultBranch(), equalTo("main"));
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequestAsList() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        assertThat(r.getMasterBranch(), equalTo("main"));
        PagedIterable<GHPullRequest> i = r.listPullRequests(GHIssueState.CLOSED);
        List<GHPullRequest> prs = i.toList();
        assertThat(prs, notNullValue());
        assertThat(prs, is(not(empty())));
    }

    @Test
    public void testGetAppInstallations() throws Exception {
        // To generate test data user-to-server OAuth access token was used
        // For more details pls read
        // https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#identifying-users-on-your-site
        final PagedIterable<GHAppInstallation> appInstallation = gitHub.getMyself().getAppInstallations();

        assertThat(appInstallation.toList(), is(not(empty())));
        assertThat(appInstallation.toList().size(), is(1));
        final GHAppInstallation ghAppInstallation = appInstallation.toList().get(0);
        assertThat(ghAppInstallation.getAppId(), is(122478L));
        assertThat(ghAppInstallation.getAccount().getLogin(), is("t0m4uk1991"));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testRepoPermissions() throws Exception {
        kohsuke();

        GHRepository r = gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
        assertThat(r.hasPullAccess(), is(true));

        r = gitHub.getOrganization("github").getRepository("hub");
        assertThat(r.hasAdminAccess(), is(false));
    }

    @Test
    public void testGetMyself() throws Exception {
        GHMyself me = gitHub.getMyself();
        assertThat(me, notNullValue());
        assertThat(me.root(), sameInstance(gitHub));
        assertThat(gitHub.getUser("bitwiseman"), notNullValue());
        PagedIterable<GHRepository> ghRepositories = me.listRepositories();
        assertThat(ghRepositories, is(not(emptyIterable())));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testPublicKeys() throws Exception {
        List<GHKey> keys = gitHub.getMyself().getPublicKeys();
        assertThat(keys, is(not(empty())));
    }

    @Test
    public void testOrgFork() throws Exception {
        cleanupRepository(GITHUB_API_TEST_ORG + "/rubywm");
        gitHub.getRepository("kohsuke/rubywm").forkTo(gitHub.getOrganization(GITHUB_API_TEST_ORG));
    }

    @Test
    public void testGetTeamsForRepo() throws Exception {
        kohsuke();
        // 'Core Developers' and 'Owners'
        assertThat(gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("testGetTeamsForRepo").getTeams().size(),
                equalTo(2));
    }

    @Test
    public void testMembership() throws Exception {
        Set<String> members = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getRepository("jenkins")
                .getCollaboratorNames();
        // System.out.println(members.contains("kohsuke"));
    }

    @Test
    public void testMemberOrgs() throws Exception {
        HashSet<GHOrganization> o = gitHub.getUser("kohsuke").getOrganizations();
        assertThat(o, hasItem(hasProperty("name", equalTo("CloudBees"))));
    }

    @Test
    public void testOrgTeams() throws Exception {
        kohsuke();
        int sz = 0;
        for (GHTeam t : gitHub.getOrganization(GITHUB_API_TEST_ORG).listTeams()) {
            assertThat(t.getName(), notNullValue());
            sz++;
        }
        assertThat(sz, lessThan(100));
    }

    @Test
    public void testOrgTeamByName() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamByName("Core Developers");
        assertThat(e, notNullValue());
    }

    @Test
    public void testOrgTeamBySlug() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug("core-developers");
        assertThat(e, notNullValue());
    }

    @Test
    public void testCommit() throws Exception {
        GHCommit commit = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .getCommit("08c1c9970af4d609ae754fbe803e06186e3206f7");
        assertThat(commit.getParents().size(), equalTo(1));
        assertThat(commit.getFiles().size(), equalTo(1));
        assertThat(commit.getHtmlUrl().toString(),
                equalTo("https://github.com/jenkinsci/jenkins/commit/08c1c9970af4d609ae754fbe803e06186e3206f7"));
        assertThat(commit.getLinesAdded(), equalTo(40));
        assertThat(commit.getLinesChanged(), equalTo(48));
        assertThat(commit.getLinesDeleted(), equalTo(8));
        assertThat(commit.getParentSHA1s().size(), equalTo(1));
        assertThat(commit.getAuthoredDate(), equalTo(GitHubClient.parseDate("2012-04-24T00:16:52Z")));
        assertThat(commit.getCommitDate(), equalTo(GitHubClient.parseDate("2012-04-24T00:16:52Z")));
        assertThat(commit.getCommitShortInfo().getCommentCount(), equalTo(0));
        assertThat(commit.getCommitShortInfo().getAuthoredDate(), equalTo(commit.getAuthoredDate()));
        assertThat(commit.getCommitShortInfo().getCommitDate(), equalTo(commit.getCommitDate()));
        assertThat(commit.getCommitShortInfo().getMessage(), equalTo("creating an RC branch"));

        File f = commit.getFiles().get(0);
        assertThat(f.getLinesChanged(), equalTo(48));
        assertThat(f.getLinesAdded(), equalTo(40));
        assertThat(f.getLinesDeleted(), equalTo(8));
        assertThat(f.getPreviousFilename(), nullValue());
        assertThat(f.getPatch(), startsWith("@@ -54,6 +54,14 @@\n"));
        assertThat(f.getSha(), equalTo("04d3e54017542ad0ff46355eababacd4850ccba5"));
        assertThat(f.getBlobUrl().toString(),
                equalTo("https://github.com/jenkinsci/jenkins/blob/08c1c9970af4d609ae754fbe803e06186e3206f7/changelog.html"));
        assertThat(f.getRawUrl().toString(),
                equalTo("https://github.com/jenkinsci/jenkins/raw/08c1c9970af4d609ae754fbe803e06186e3206f7/changelog.html"));

        assertThat(f.getStatus(), equalTo("modified"));
        assertThat(f.getFileName(), equalTo("changelog.html"));

        // walk the tree
        GHTree t = commit.getTree();
        assertThat(IOUtils.toString(t.getEntry("todo.txt").readAsBlob()), containsString("executor rendering"));
        assertThat(t.getEntry("war").asTree(), notNullValue());
    }

    @Test
    public void testListCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("kohsuke").getRepository("empty-commit").listCommits()) {
            sha1.add(c.getSHA1());
        }
        assertThat(sha1.get(0), equalTo("fdfad6be4db6f96faea1f153fb447b479a7a9cb7"));
        assertThat(sha1.size(), equalTo(1));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testBranches() throws Exception {
        Map<String, GHBranch> b = gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
        // System.out.println(b);
    }

    @Test
    public void testCommitComment() throws Exception {
        GHRepository r = gitHub.getUser("jenkinsci").getRepository("jenkins");
        PagedIterable<GHCommitComment> comments = r.listCommitComments();
        List<GHCommitComment> batch = comments.iterator().nextPage();
        for (GHCommitComment comment : batch) {
            // System.out.println(comment.getBody());
            assertThat(r, sameInstance(comment.getOwner()));
        }
    }

    @Test
    public void testCreateCommitComment() throws Exception {
        GHCommit commit = gitHub.getUser("kohsuke")
                .getRepository("sandbox-ant")
                .getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");

        assertThat(commit.getCommitShortInfo().getCommentCount(), equalTo(30));
        GHCommitComment c = commit.createComment("[testing](http://kohsuse.org/)");
        try {
            assertThat(c.getPath(), nullValue());
            assertThat(c.getLine(), equalTo(-1));
            assertThat(c.getHtmlUrl().toString(),
                    containsString(
                            "kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000#commitcomment-"));
            assertThat(c.listReactions().toList(), is(empty()));

            c.update("updated text");
            assertThat(c.getBody(), equalTo("updated text"));

            commit = gitHub.getUser("kohsuke")
                    .getRepository("sandbox-ant")
                    .getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");

            assertThat(commit.getCommitShortInfo().getCommentCount(), equalTo(31));

            // testing reactions
            List<GHReaction> reactions = c.listReactions().toList();
            assertThat(reactions, is(empty()));

            GHReaction reaction = c.createReaction(ReactionContent.CONFUSED);
            assertThat(reaction.getContent(), equalTo(ReactionContent.CONFUSED));

            reactions = c.listReactions().toList();
            assertThat(reactions.size(), equalTo(1));

            c.deleteReaction(reaction);

            reactions = c.listReactions().toList();
            assertThat(reactions.size(), equalTo(0));
        } finally {
            c.delete();
        }
    }

    @Test
    public void tryHook() throws Exception {
        final GHOrganization o = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        final GHRepository r = o.getRepository("github-api");
        try {
            GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
            assertThat(hook.getName(), equalTo("web"));
            assertThat(hook.getEvents().size(), equalTo(1));
            assertThat(hook.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook.getConfig().size(), equalTo(3));
            assertThat(hook.isActive(), equalTo(true));

            GHHook hook2 = r.getHook((int) hook.getId());
            assertThat(hook2.getName(), equalTo("web"));
            assertThat(hook2.getEvents().size(), equalTo(1));
            assertThat(hook2.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook2.getConfig().size(), equalTo(3));
            assertThat(hook2.isActive(), equalTo(true));
            hook2.ping();
            hook2.delete();
            final GHHook finalRepoHook = hook;
            GHFileNotFoundException e = Assert.assertThrows(GHFileNotFoundException.class,
                    () -> r.getHook((int) finalRepoHook.getId()));
            assertThat(e.getMessage(),
                    containsString("repos/hub4j-test-org/github-api/hooks/" + finalRepoHook.getId()));
            assertThat(e.getMessage(), containsString("rest/reference/repos#get-a-repository-webhook"));

            hook = r.createWebHook(new URL("http://www.google.com/"));
            r.deleteHook((int) hook.getId());

            hook = o.createWebHook(new URL("http://www.google.com/"));
            assertThat(hook.getName(), equalTo("web"));
            assertThat(hook.getEvents().size(), equalTo(1));
            assertThat(hook.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook.getConfig().size(), equalTo(3));
            assertThat(hook.isActive(), equalTo(true));

            hook2 = o.getHook((int) hook.getId());
            assertThat(hook2.getName(), equalTo("web"));
            assertThat(hook2.getEvents().size(), equalTo(1));
            assertThat(hook2.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook2.getConfig().size(), equalTo(3));
            assertThat(hook2.isActive(), equalTo(true));
            hook2.ping();
            hook2.delete();

            final GHHook finalOrgHook = hook;
            GHFileNotFoundException e2 = Assert.assertThrows(GHFileNotFoundException.class,
                    () -> o.getHook((int) finalOrgHook.getId()));
            assertThat(e2.getMessage(), containsString("orgs/hub4j-test-org/hooks/" + finalOrgHook.getId()));
            assertThat(e2.getMessage(), containsString("rest/reference/orgs#get-an-organization-webhook"));

            hook = o.createWebHook(new URL("http://www.google.com/"));
            o.deleteHook((int) hook.getId());

            // System.out.println(hook);
        } finally {
            if (mockGitHub.isUseProxy()) {
                GHRepository cleanupRepo = getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG)
                        .getRepository("github-api");
                for (GHHook h : cleanupRepo.getHooks()) {
                    h.delete();
                }
            }
        }
    }

    @Test
    public void testEventApi() throws Exception {
        for (GHEventInfo ev : gitHub.getEvents()) {
            if (ev.getType() == GHEvent.PULL_REQUEST) {
                if (ev.getId() == 10680625394L) {
                    assertThat(ev.getActorLogin(), equalTo("pull[bot]"));
                    assertThat(ev.getOrganization(), nullValue());
                    assertThat(ev.getRepository().getFullName(), equalTo("daddyfatstacksBIG/lerna"));
                    assertThat(ev.getCreatedAt(), equalTo(GitHubClient.parseDate("2019-10-21T21:54:52Z")));
                    assertThat(ev.getType(), equalTo(GHEvent.PULL_REQUEST));
                }

                GHEventPayload.PullRequest pr = ev.getPayload(GHEventPayload.PullRequest.class);
                assertThat(pr.getNumber(), is(pr.getPullRequest().getNumber()));
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testApp() throws IOException {
        // System.out.println(gitHub.getMyself().getEmails());

        // GHRepository r = gitHub.getOrganization("jenkinsci").createRepository("kktest4", "Kohsuke's test",
        // "http://kohsuke.org/", "Everyone", true);
        // r.fork();

        // tryDisablingIssueTrackers(gitHub);

        // tryDisablingWiki(gitHub);

        // GHPullRequest i = gitHub.getOrganization("jenkinsci").getRepository("sandbox").getPullRequest(1);
        // for (GHIssueComment c : i.getComments())
        // // System.out.println(c);
        // // System.out.println(i);

        // gitHub.getMyself().getRepository("perforce-plugin").setEmailServiceHook("kk@kohsuke.org");

        // tryRenaming(gitHub);
        // tryOrgFork(gitHub);

        // testOrganization(gitHub);
        // testPostCommitHook(gitHub);

        // tryTeamCreation(gitHub);

        // t.add(gitHub.getMyself());
        // // System.out.println(t.getMembers());
        // t.remove(gitHub.getMyself());
        // // System.out.println(t.getMembers());

        // GHRepository r = gitHub.getOrganization("HudsonLabs").createRepository("auto-test", "some description",
        // "http://kohsuke.org/", "Plugin Developers", true);

        // r.
        // GitHub hub = GitHub.connectAnonymously();
        //// hub.createRepository("test","test repository",null,true);
        //// hub.getUserTest("kohsuke").getRepository("test").delete();
        //
        // // System.out.println(hub.getUserTest("kohsuke").getRepository("hudson").getCollaborators());
    }

    private void tryDisablingIssueTrackers(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasIssues()) {
                if (r.getOpenIssueCount() == 0) {
                    // System.out.println("DISABLED " + r.getName());
                    r.enableIssueTracker(false);
                } else {
                    // System.out.println("UNTOUCHED " + r.getName());
                }
            }
        }
    }

    private void tryDisablingWiki(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasWiki()) {
                // System.out.println("DISABLED " + r.getName());
                r.enableWiki(false);
            }
        }
    }

    private void tryUpdatingIssueTracker(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("lib-task-reactor");
        // System.out.println(r.hasIssues());
        // System.out.println(r.getOpenIssueCount());
        r.enableIssueTracker(false);
    }

    private void tryRenaming(GitHub gitHub) throws IOException {
        gitHub.getUser("kohsuke").getRepository("test").renameTo("test2");
    }

    private void tryTeamCreation(GitHub gitHub) throws IOException {
        GHOrganization o = gitHub.getOrganization("HudsonLabs");
        GHTeam t = o.createTeam("auto team", Permission.PUSH);
        t.add(o.getRepository("auto-test"));
    }

    private void testPostCommitHook(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getMyself().getRepository("foo");
        Set<URL> hooks = r.getPostCommitHooks();
        hooks.add(new URL("http://kohsuke.org/test"));
        // System.out.println(hooks);
        hooks.remove(new URL("http://kohsuke.org/test"));
        // System.out.println(hooks);
    }

    @Test
    public void testOrgRepositories() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        long start = System.currentTimeMillis();
        Map<String, GHRepository> repos = j.getRepositories();
        long end = System.currentTimeMillis();
        // System.out.printf("%d repositories in %dms\n", repos.size(), end - start);
    }

    @Test
    public void testOrganization() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam t = j.getTeams().get("Core Developers");

        assertThat(j.getRepository("jenkins"), notNullValue());

        // t.add(labs.getRepository("xyz"));
    }

    @Test
    public void testCommitStatus() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");

        GHCommitStatus state;

        // state = r.createCommitStatus("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396", GHCommitState.FAILURE,
        // "http://kohsuke.org/", "testing!");

        List<GHCommitStatus> lst = r.listCommitStatuses("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396").toList();
        state = lst.get(0);
        // System.out.println(state);
        assertThat(state.getDescription(), equalTo("testing!"));
        assertThat(state.getTargetUrl(), equalTo("http://kohsuke.org/"));
        assertThat(state.getCreator().getLogin(), equalTo("kohsuke"));
    }

    @Test
    public void testCommitShortInfo() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f23");
        assertThat("Kohsuke Kawaguchi", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat("doc", equalTo(commit.getCommitShortInfo().getMessage()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNSIGNED, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
        assertThat(commit.getCommitShortInfo().getAuthor().getDate().toInstant().getEpochSecond(),
                equalTo(1271650361L));
        assertThat(commit.getCommitShortInfo().getCommitter().getDate().toInstant().getEpochSecond(),
                equalTo(1271650361L));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testPullRequestPopulate() throws Exception {
        GHRepository r = gitHub.getUser("kohsuke").getRepository("github-api");
        GHPullRequest p = r.getPullRequest(17);
        GHUser u = p.getUser();
        assertThat(u.getName(), notNullValue());
    }

    @Test
    public void testCheckMembership() throws Exception {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        GHUser kohsuke = gitHub.getUser("kohsuke");
        GHUser b = gitHub.getUser("b");

        assertThat(j.hasMember(kohsuke), is(true));
        assertThat(j.hasMember(b), is(false));

        assertThat(j.hasPublicMember(kohsuke), is(true));
        assertThat(j.hasPublicMember(b), is(false));
    }

    @Test
    public void testRef() throws IOException {
        GHRef mainRef = gitHub.getRepository("jenkinsci/jenkins").getRef("heads/main");
        assertThat(mainRef.getUrl().toString(),
                equalTo(mockGitHub.apiServer().baseUrl() + "/repos/jenkinsci/jenkins/git/refs/heads/main"));
    }

    @Test
    public void directoryListing() throws IOException {
        List<GHContent> children = gitHub.getRepository("jenkinsci/jenkins").getDirectoryContent("core");
        for (GHContent c : children) {
            // System.out.println(c.getName());
            if (c.isDirectory()) {
                for (GHContent d : c.listDirectoryContent()) {
                    // System.out.println(" " + d.getName());
                }
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testAddDeployKey() throws IOException {
        GHRepository myRepository = getTestRepository();
        final GHDeployKey newDeployKey = myRepository.addDeployKey("test",
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDUt0RAycC5cS42JKh6SecfFZBR1RrF+2hYMctz4mk74/arBE+wFb7fnSHGzdGKX2h5CFOWODifRCJVhB7hlVxodxe+QkQQYAEL/x1WVCJnGgTGQGOrhOMj95V3UE5pQKhsKD608C+u5tSofcWXLToP1/wZ7U4/AHjqYi08OLsWToHCax55TZkvdt2jo0hbIoYU+XI9Q8Uv4ONDN1oabiOdgeKi8+crvHAuvNleiBhWVBzFh8KdfzaH5uNdw7ihhFjEd1vzqACsjCINCjdMfzl6jD9ExuWuE92nZJnucls2cEoNC6k2aPmrZDg9hA32FXVpyseY+bDUWFU6LO2LG6PB kohsuke@atlas");
        try {
            assertThat(newDeployKey.getId(), notNullValue());

            GHDeployKey k = Iterables.find(myRepository.getDeployKeys(), new Predicate<GHDeployKey>() {
                public boolean apply(GHDeployKey deployKey) {
                    return newDeployKey.getId() == deployKey.getId();
                }
            });
            assertThat(k, notNullValue());
        } finally {
            newDeployKey.delete();
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testCommitStatusContext() throws IOException {
        GHRepository myRepository = getTestRepository();
        GHRef mainRef = myRepository.getRef("heads/main");
        GHCommitStatus commitStatus = myRepository.createCommitStatus(mainRef.getObject()
                .getSha(), GHCommitState.SUCCESS, "http://www.example.com", "test", "test/context");
        assertThat(commitStatus.getContext(), equalTo("test/context"));

    }

    @Ignore("Needs mocking check")
    @Test
    public void testMemberPagenation() throws IOException {
        Set<GHUser> all = new HashSet<GHUser>();
        for (GHUser u : gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamByName("Core Developers").listMembers()) {
            // System.out.println(u.getLogin());
            all.add(u);
        }
        assertThat(all, is(not(empty())));
    }

    @Test
    public void testCommitSearch() throws IOException {
        PagedSearchIterable<GHCommit> r = gitHub.searchCommits()
                .org("github-api")
                .repo("github-api")
                .author("kohsuke")
                .sort(GHCommitSearchBuilder.Sort.COMMITTER_DATE)
                .list();
        assertThat(r.getTotalCount(), greaterThan(0));

        GHCommit firstCommit = r.iterator().next();
        assertThat(firstCommit.getFiles(), is(not(empty())));
    }

    @Test
    public void testIssueSearch() throws IOException {
        PagedSearchIterable<GHIssue> r = gitHub.searchIssues()
                .mentions("kohsuke")
                .isOpen()
                .sort(GHIssueSearchBuilder.Sort.UPDATED)
                .list();
        assertThat(r.getTotalCount(), greaterThan(0));
        for (GHIssue issue : r) {
            assertThat(issue.getTitle(), notNullValue());
            PagedIterable<GHIssueComment> comments = issue.listComments();
            for (GHIssueComment comment : comments) {
                assertThat(comment, notNullValue());
            }
        }
    }

    @Test // issue #99
    public void testReadme() throws IOException {
        GHContent readme = gitHub.getRepository("hub4j-test-org/test-readme").getReadme();
        assertThat("README.md", equalTo(readme.getName()));
        assertThat("This is a markdown readme.\n", equalTo(readme.getContent()));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testTrees() throws IOException {
        GHTree mainTree = gitHub.getRepository("hub4j/github-api").getTree("main");
        boolean foundReadme = false;
        for (GHTreeEntry e : mainTree.getTree()) {
            if ("readme".equalsIgnoreCase(e.getPath().replaceAll("\\.md", ""))) {
                foundReadme = true;
                break;
            }
        }
        assertThat(foundReadme, is(true));
    }

    @Test
    public void testTreesRecursive() throws IOException {
        GHTree mainTree = gitHub.getRepository("hub4j/github-api").getTreeRecursive("main", 1);
        boolean foundThisFile = false;
        for (GHTreeEntry e : mainTree.getTree()) {
            if (e.getPath().endsWith(AppTest.class.getSimpleName() + ".java")) {
                foundThisFile = true;
                assertThat(e.getPath(), equalTo("src/test/java/org/kohsuke/github/AppTest.java"));
                assertThat(e.getSha(), equalTo("baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                assertThat(e.getMode(), equalTo("100755"));
                assertThat(e.getSize(), greaterThan(30000L));
                assertThat(e.getUrl().toString(),
                        containsString("/repos/hub4j/github-api/git/blobs/baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                GHBlob blob = e.asBlob();
                assertThat(e.asBlob().getUrl().toString(),
                        containsString("/repos/hub4j/github-api/git/blobs/baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                break;
            }

        }
        assertThat(foundThisFile, is(true));
    }

    @Test
    public void testRepoLabel() throws IOException {
        cleanupLabel("test");
        cleanupLabel("test2");

        GHRepository r = gitHub.getRepository("hub4j-test-org/test-labels");
        List<GHLabel> lst = r.listLabels().toList();
        for (GHLabel l : lst) {
            assertThat(l.getUrl(), containsString(l.getName().replace(" ", "%20")));
        }
        assertThat(lst.size(), greaterThan(5));
        GHLabel e = r.getLabel("enhancement");
        assertThat(e.getName(), equalTo("enhancement"));
        assertThat(e.getUrl(), notNullValue());
        assertThat(e.getId(), equalTo(177339106L));
        assertThat(e.getNodeId(), equalTo("MDU6TGFiZWwxNzczMzkxMDY="));
        assertThat(e.isDefault(), is(true));
        assertThat(e.getColor(), matchesPattern("[0-9a-fA-F]{6}"));

        GHLabel t = null;
        GHLabel t2 = null;
        GHLabel t3 = null;
        try {// CRUD
            t = r.createLabel("test", "123456");
            t2 = r.getLabel("test");
            assertThat(t, not(sameInstance(t2)));
            assertThat(t, equalTo(t2));

            assertThat(t2.isDefault(), is(false));

            assertThat(t2.getId(), equalTo(t.getId()));
            assertThat(t2.getNodeId(), equalTo(t.getNodeId()));
            assertThat(t2.getName(), equalTo(t.getName()));
            assertThat("123456", equalTo(t.getColor()));
            assertThat(t2.getColor(), equalTo(t.getColor()));
            assertThat("", equalTo(t.getDescription()));
            assertThat(t2.getDescription(), equalTo(t.getDescription()));
            assertThat(t2.getUrl(), equalTo(t.getUrl()));
            assertThat(t2.isDefault(), equalTo(t.isDefault()));

            // update works on multiple changes in one call
            t3 = t.update().color("000000").description("It is dark!").done();

            // instances behave as immutable by default. Update returns a new updated instance.
            assertThat(t, not(sameInstance(t2)));
            assertThat(t, equalTo(t2));

            assertThat(t, not(sameInstance(t3)));
            assertThat(t, not(equalTo(t3)));

            assertThat("123456", equalTo(t.getColor()));
            assertThat("", equalTo(t.getDescription()));
            assertThat("000000", equalTo(t3.getColor()));
            assertThat("It is dark!", equalTo(t3.getDescription()));

            // Test deprecated methods
            t.setDescription("Deprecated");
            t = r.getLabel("test");

            // By using the old instance t when calling setDescription it also sets color to the old value
            // this is a bad behavior, but it is expected
            assertThat("123456", equalTo(t.getColor()));
            assertThat("Deprecated", equalTo(t.getDescription()));

            t.setColor("000000");
            t = r.getLabel("test");
            assertThat("000000", equalTo(t.getColor()));
            assertThat("Deprecated", equalTo(t.getDescription()));

            // set() makes a single change
            t3 = t.set().description("this is also a test");

            // instances behave as immutable by default. Update returns a new updated instance.
            assertThat(t, not(sameInstance(t3)));
            assertThat(t, not(equalTo(t3)));

            assertThat("000000", equalTo(t3.getColor()));
            assertThat("this is also a test", equalTo(t3.getDescription()));

            t.delete();
            try {
                t = r.getLabel("test");
                fail("Test label should be deleted.");
            } catch (IOException ex) {
                assertThat(ex, instanceOf(FileNotFoundException.class));
            }

            t = r.createLabel("test2", "123457", "this is a different test");
            t2 = r.getLabel("test2");

            assertThat(t2.getName(), equalTo(t.getName()));
            assertThat("123457", equalTo(t.getColor()));
            assertThat(t2.getColor(), equalTo(t.getColor()));
            assertThat("this is a different test", equalTo(t.getDescription()));
            assertThat(t2.getDescription(), equalTo(t.getDescription()));
            assertThat(t2.getUrl(), equalTo(t.getUrl()));
            t.delete();

            // Allow null description
            t = GHLabel.create(r).name("test2").color("123458").done();
            assertThat(t.getName(), equalTo("test2"));
            assertThat(t.getDescription(), is(nullValue()));

        } finally {
            cleanupLabel("test");
            cleanupLabel("test2");
        }
    }

    void cleanupLabel(String name) {
        if (mockGitHub.isUseProxy()) {
            try {
                GHLabel t = getNonRecordingGitHub().getRepository("hub4j-test-org/test-labels").getLabel(name);
                t.delete();
            } catch (IOException e) {

            }
        }
    }

    @Test
    public void testSubscribers() throws IOException {
        boolean bitwiseman = false;
        GHRepository mr = gitHub.getRepository("bitwiseman/github-api");
        for (GHUser u : mr.listSubscribers()) {
            bitwiseman |= u.getLogin().equals("bitwiseman");
        }
        assertThat(bitwiseman, is(true));

        boolean githubApiFound = false;
        for (GHRepository r : gitHub.getUser("bitwiseman").listRepositories()) {
            githubApiFound |= r.equals(mr);
        }
        assertThat(githubApiFound, is(true));
    }

    @Test
    public void notifications() throws Exception {
        boolean found = false;
        for (GHThread t : gitHub.listNotifications().nonBlocking(true).read(true)) {
            if (!found) {
                found = true;
                // both read and unread are included
                assertThat(t.getTitle(), is("Create a Jenkinsfile for Librecores CI in mor1kx"));
                assertThat(t.getLastReadAt(), notNullValue());
                assertThat(t.isRead(), equalTo(true));

                t.markAsRead(); // test this by calling it once on old notfication
            }
            assertThat(t.getReason(), oneOf("subscribed", "mention", "review_requested", "comment"));
            assertThat(t.getTitle(), notNullValue());
            assertThat(t.getLastCommentUrl(), notNullValue());
            assertThat(t.getRepository(), notNullValue());
            assertThat(t.getUpdatedAt(), notNullValue());
            assertThat(t.getType(), oneOf("Issue", "PullRequest"));

            // both thread an unread are included
            // assertThat(t.getLastReadAt(), notNullValue());
            // assertThat(t.isRead(), equalTo(true));

            // Doesn't exist on threads but is part of GHObject. :(
            assertThat(t.getCreatedAt(), nullValue());

        }
        assertThat(found, is(true));
        gitHub.listNotifications().markAsRead();
    }

    @Ignore("Needs mocking check")
    @Test
    public void checkToString() throws Exception {
        // Just basic code coverage to make sure toString() doesn't blow up
        GHUser u = gitHub.getUser("rails");
        // System.out.println(u);
        GHRepository r = u.getRepository("rails");
        // System.out.println(r);
        // System.out.println(r.getIssue(1));
    }

    @Test
    public void reactions() throws Exception {
        GHIssue i = gitHub.getRepository("hub4j/github-api").getIssue(311);

        List<GHReaction> l;
        // retrieval
        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));

        assertThat(l.get(0).getUser().getLogin(), is("kohsuke"));
        assertThat(l.get(0).getContent(), is(ReactionContent.HEART));

        // CRUD
        GHReaction a;
        a = i.createReaction(ReactionContent.HOORAY);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.HOORAY));
        i.deleteReaction(a);

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));

        a = i.createReaction(ReactionContent.PLUS_ONE);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.PLUS_ONE));

        a = i.createReaction(ReactionContent.CONFUSED);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.CONFUSED));

        a = i.createReaction(ReactionContent.EYES);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.EYES));

        a = i.createReaction(ReactionContent.ROCKET);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.ROCKET));

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(5));
        assertThat(l.get(0).getUser().getLogin(), is("kohsuke"));
        assertThat(l.get(0).getContent(), is(ReactionContent.HEART));
        assertThat(l.get(1).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(1).getContent(), is(ReactionContent.PLUS_ONE));
        assertThat(l.get(2).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(2).getContent(), is(ReactionContent.CONFUSED));
        assertThat(l.get(3).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(3).getContent(), is(ReactionContent.EYES));
        assertThat(l.get(4).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(4).getContent(), is(ReactionContent.ROCKET));

        i.deleteReaction(l.get(1));
        i.deleteReaction(l.get(2));
        i.deleteReaction(l.get(3));
        i.deleteReaction(l.get(4));

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));
    }

    @Test
    public void listOrgMemberships() throws Exception {
        GHMyself me = gitHub.getMyself();
        for (GHMembership m : me.listOrgMemberships()) {
            assertThat(m.getUser(), is((GHUser) me));
            assertThat(m.getState(), notNullValue());
            assertThat(m.getRole(), notNullValue());
        }
    }

    @Test
    public void blob() throws Exception {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        GHRepository r = gitHub.getRepository("hub4j/github-api");
        String sha1 = "a12243f2fc5b8c2ba47dd677d0b0c7583539584d";

        verifyBlobContent(r.readBlob(sha1));

        GHBlob blob = r.getBlob(sha1);
        verifyBlobContent(blob.read());
        assertThat(blob.getSha(), is("a12243f2fc5b8c2ba47dd677d0b0c7583539584d"));
        assertThat(blob.getSize(), is(1104L));
    }

    private void verifyBlobContent(InputStream is) throws Exception {
        String content = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
        assertThat(content, containsString("Copyright (c) 2011- Kohsuke Kawaguchi and other contributors"));
        assertThat(content, containsString("FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR"));
        assertThat(content.length(), is(1104));
    }
}

package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.kohsuke.github.example.dataobject.ReadOnlyObjects;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.kohsuke.github.GHMarketplaceAccountType.ORGANIZATION;

/**
 * Unit test for {@link GitHub}.
 */
public class GitHubTest extends AbstractGitHubWireMockTest {

    @Test
    public void listUsers() throws IOException {
        for (GHUser u : Iterables.limit(gitHub.listUsers(), 10)) {
            assert u.getName() != null;
            // System.out.println(u.getName());
        }
    }

    @Test
    public void getRepository() throws IOException {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");

        assertThat(repo.getFullName(), equalTo("hub4j/github-api"));

        GHRepository repo2 = gitHub.getRepositoryById(Long.toString(repo.getId()));
        assertThat(repo2.getFullName(), equalTo("hub4j/github-api"));

        try {
            gitHub.getRepository("hub4j_github-api");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Repository name must be in format owner/repo"));
        }
    }

    @Test
    public void getOrgs() throws IOException {
        int iterations = 10;
        Set<Long> orgIds = new HashSet<Long>();
        for (GHOrganization org : Iterables.limit(gitHub.listOrganizations().withPageSize(2), iterations)) {
            orgIds.add(org.getId());
            // System.out.println(org.getName());
        }
        assertThat(orgIds.size(), equalTo(iterations));

        GHOrganization org = gitHub.getOrganization("hub4j");
        GHOrganization org2 = gitHub.getOrganization("hub4j");
        assertThat(org.getLogin(), equalTo("hub4j"));
        // caching
        assertThat(org, sameInstance(org2));

        gitHub.refreshCache();
        org2 = gitHub.getOrganization("hub4j");
        assertThat(org2.getLogin(), equalTo("hub4j"));
        // cache cleared
        assertThat(org, not(sameInstance(org2)));
    }

    @Test
    public void searchUsers() throws Exception {
        PagedSearchIterable<GHUser> r = gitHub.searchUsers().q("tom").repos(">42").followers(">1000").list();
        GHUser u = r.iterator().next();
        // System.out.println(u.getName());
        assertThat(u.getId(), notNullValue());
        assertThat(r.getTotalCount(), greaterThan(0));
    }

    @Test
    public void testListAllRepositories() throws Exception {
        Iterator<GHRepository> itr = gitHub.listAllPublicRepositories().iterator();
        for (int i = 0; i < 115; i++) {
            assertThat(itr.hasNext(), is(true));
            GHRepository r = itr.next();
            // System.out.println(r.getFullName());
            assertThat(r.getUrl(), notNullValue());
            assertThat(r.getId(), not(0L));
        }

        // ensure the iterator throws as expected
        try {
            itr.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, notNullValue());
        }
    }

    @Test
    public void searchContent() throws Exception {
        PagedSearchIterable<GHContent> r = gitHub.searchContent()
                .q("addClass")
                .in("file")
                .language("js")
                .repo("jquery/jquery")
                // ignored unless sort is also set
                .order(GHDirection.DESC)
                .list();
        GHContent c = r.iterator().next();

        // System.out.println(c.getName());
        assertThat(c.getDownloadUrl(), notNullValue());
        assertThat(c.getOwner(), notNullValue());
        assertThat(c.getOwner().getFullName(), equalTo("jquery/jquery"));
        assertThat(r.getTotalCount(), greaterThan(5));

        PagedSearchIterable<GHContent> r2 = gitHub.searchContent()
                .q("addClass")
                .in("file")
                .language("js")
                .repo("jquery/jquery")
                // resets query sort back to default
                .sort(GHContentSearchBuilder.Sort.INDEXED)
                .sort(GHContentSearchBuilder.Sort.BEST_MATCH)
                // ignored unless sort is also set to non-default
                .order(GHDirection.ASC)
                .list();

        GHContent c2 = r2.iterator().next();
        assertThat(c2.getPath(), equalTo(c.getPath()));
        assertThat(r2.getTotalCount(), equalTo(r.getTotalCount()));

        PagedSearchIterable<GHContent> r3 = gitHub.searchContent()
                .q("addClass")
                .in("file")
                .language("js")
                .repo("jquery/jquery")
                .sort(GHContentSearchBuilder.Sort.INDEXED)
                .order(GHDirection.ASC)
                .list();

        GHContent c3 = r3.iterator().next();
        assertThat(c3.getPath(), not(equalTo(c2.getPath())));
        assertThat(r3.getTotalCount(), equalTo(r2.getTotalCount()));

        PagedSearchIterable<GHContent> r4 = gitHub.searchContent()
                .q("addClass")
                .in("file")
                .language("js")
                .repo("jquery/jquery")
                .sort(GHContentSearchBuilder.Sort.INDEXED)
                .order(GHDirection.DESC)
                .list();

        GHContent c4 = r4.iterator().next();
        assertThat(c4.getPath(), not(equalTo(c2.getPath())));
        assertThat(c4.getPath(), not(equalTo(c3.getPath())));
        assertThat(r4.getTotalCount(), equalTo(r2.getTotalCount()));

    }

    @Test
    public void searchContentWithForks() {
        final PagedSearchIterable<GHContent> results = gitHub.searchContent()
                .q("addClass")
                .language("js")
                .sort(GHContentSearchBuilder.Sort.INDEXED)
                .order(GHDirection.DESC)
                .list();

        final PagedSearchIterable<GHContent> resultsWithForks = gitHub.searchContent()
                .q("addClass")
                .language("js")
                .sort(GHContentSearchBuilder.Sort.INDEXED)
                .order(GHDirection.DESC)
                .fork(GHFork.PARENT_AND_FORKS)
                .list();

        assertThat(results.getTotalCount(), lessThan(resultsWithForks.getTotalCount()));
    }

    @Test
    public void testListMyAuthorizations() throws IOException {
        PagedIterable<GHAuthorization> list = gitHub.listMyAuthorizations();

        for (GHAuthorization auth : list) {
            assertThat(auth.getAppName(), notNullValue());
        }
    }

    @Test
    public void getMeta() throws IOException {
        GHMeta meta = gitHub.getMeta();
        assertThat(meta.isVerifiablePasswordAuthentication(), is(true));
        assertThat(meta.getApi().size(), equalTo(19));
        assertThat(meta.getGit().size(), equalTo(36));
        assertThat(meta.getHooks().size(), equalTo(4));
        assertThat(meta.getImporter().size(), equalTo(3));
        assertThat(meta.getPages().size(), equalTo(6));
        assertThat(meta.getWeb().size(), equalTo(20));
        assertThat(meta.getPackages().size(), equalTo(25));
        assertThat(meta.getActions().size(), equalTo(1739));
        assertThat(meta.getDependabot().size(), equalTo(3));

        // Also test examples here
        Class[] examples = new Class[]{ ReadOnlyObjects.GHMetaPublic.class, ReadOnlyObjects.GHMetaPackage.class,
                ReadOnlyObjects.GHMetaGettersUnmodifiable.class, ReadOnlyObjects.GHMetaGettersFinal.class,
                ReadOnlyObjects.GHMetaGettersFinalCreator.class, };

        for (Class metaClass : examples) {
            ReadOnlyObjects.GHMetaExample metaExample = gitHub.createRequest()
                    .withUrlPath("/meta")
                    .fetch((Class<ReadOnlyObjects.GHMetaExample>) metaClass);
            assertThat(metaExample.isVerifiablePasswordAuthentication(), is(true));
            assertThat(metaExample.getApi().size(), equalTo(19));
            assertThat(metaExample.getGit().size(), equalTo(36));
            assertThat(metaExample.getHooks().size(), equalTo(4));
            assertThat(metaExample.getImporter().size(), equalTo(3));
            assertThat(metaExample.getPages().size(), equalTo(6));
            assertThat(metaExample.getWeb().size(), equalTo(20));
        }
    }

    @Test
    public void getMyMarketplacePurchases() throws IOException {
        List<GHMarketplaceUserPurchase> userPurchases = gitHub.getMyMarketplacePurchases().toList();
        assertThat(userPurchases.size(), equalTo(2));

        for (GHMarketplaceUserPurchase userPurchase : userPurchases) {
            assertThat(userPurchase.isOnFreeTrial(), is(false));
            assertThat(userPurchase.getFreeTrialEndsOn(), nullValue());
            assertThat(userPurchase.getBillingCycle(), equalTo("monthly"));

            GHMarketplacePlan plan = userPurchase.getPlan();
            // GHMarketplacePlan - Non-nullable fields
            assertThat(plan.getUrl(), notNullValue());
            assertThat(plan.getAccountsUrl(), notNullValue());
            assertThat(plan.getName(), notNullValue());
            assertThat(plan.getDescription(), notNullValue());
            assertThat(plan.getPriceModel(), notNullValue());
            assertThat(plan.getState(), notNullValue());

            // GHMarketplacePlan - primitive fields
            assertThat(plan.getId(), not(0L));
            assertThat(plan.getNumber(), not(0L));
            assertThat(plan.getMonthlyPriceInCents(), greaterThanOrEqualTo(0L));

            // GHMarketplacePlan - list
            assertThat(plan.getBullets().size(), equalTo(2));

            GHMarketplaceAccount account = userPurchase.getAccount();
            // GHMarketplaceAccount - Non-nullable fields
            assertThat(account.getLogin(), notNullValue());
            assertThat(account.getUrl(), notNullValue());
            assertThat(account.getType(), notNullValue());

            // GHMarketplaceAccount - primitive fields
            assertThat(account.getId(), not(0L));

            /* logical combination tests */
            // Rationale: organization_billing_email is only set when account type is ORGANIZATION.
            if (account.getType() == ORGANIZATION)
                assertThat(account.getOrganizationBillingEmail(), notNullValue());
            else
                assertThat(account.getOrganizationBillingEmail(), nullValue());
        }
    }

    @Test
    public void gzip() throws Exception {

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        // getResponseHeaderFields is deprecated but we'll use it for testing.
        assertThat(org.getResponseHeaderFields(), notNullValue());

        // WireMock should automatically gzip all responses
        assertThat(org.getResponseHeaderFields().get("Content-Encoding").get(0), is("gzip"));
        assertThat(org.getResponseHeaderFields().get("Content-eNcoding").get(0), is("gzip"));
    }

    @Test
    public void testHeaderFieldName() throws Exception {

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        // getResponseHeaderFields is deprecated but we'll use it for testing.
        assertThat(org.getResponseHeaderFields(), notNullValue());

        assertThat("Header field names must be case-insensitive",
                org.getResponseHeaderFields().containsKey("CacHe-ContrOl"));

        assertThat("KeySet from header fields should also be case-insensitive",
                org.getResponseHeaderFields().keySet().contains("CacHe-ControL"));
        assertThat(org.getResponseHeaderFields().get("cachE-cOntrol").get(0), is("private, max-age=60, s-maxage=60"));
    }
}

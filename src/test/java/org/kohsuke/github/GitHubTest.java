package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.kohsuke.github.example.dataobject.ReadOnlyObjects;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
    public void getOrgs() throws IOException {
        int iterations = 10;
        Set<Long> orgIds = new HashSet<Long>();
        for (GHOrganization org : Iterables.limit(gitHub.listOrganizations().withPageSize(2), iterations)) {
            orgIds.add(org.getId());
            // System.out.println(org.getName());
        }
        assertThat(orgIds.size(), equalTo(iterations));
    }

    @Test
    public void searchUsers() throws Exception {
        PagedSearchIterable<GHUser> r = gitHub.searchUsers().q("tom").repos(">42").followers(">1000").list();
        GHUser u = r.iterator().next();
        // System.out.println(u.getName());
        assertNotNull(u.getId());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void testListAllRepositories() throws Exception {
        Iterator<GHRepository> itr = gitHub.listAllPublicRepositories().iterator();
        for (int i = 0; i < 115; i++) {
            assertTrue(itr.hasNext());
            GHRepository r = itr.next();
            // System.out.println(r.getFullName());
            assertNotNull(r.getUrl());
            assertNotEquals(0L, r.getId());
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
                .list();
        GHContent c = r.iterator().next();
        // System.out.println(c.getName());
        assertNotNull(c.getDownloadUrl());
        assertNotNull(c.getOwner());
        assertEquals("jquery/jquery", c.getOwner().getFullName());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void testListMyAuthorizations() throws IOException {
        PagedIterable<GHAuthorization> list = gitHub.listMyAuthorizations();

        for (GHAuthorization auth : list) {
            assertNotNull(auth.getAppName());
        }
    }

    @Test
    public void getMeta() throws IOException {
        GHMeta meta = gitHub.getMeta();
        assertTrue(meta.isVerifiablePasswordAuthentication());
        assertEquals(19, meta.getApi().size());
        assertEquals(19, meta.getGit().size());
        assertEquals(3, meta.getHooks().size());
        assertEquals(6, meta.getImporter().size());
        assertEquals(6, meta.getPages().size());
        assertEquals(19, meta.getWeb().size());

        // Also test examples here
        Class[] examples = new Class[]{ ReadOnlyObjects.GHMetaPublic.class, ReadOnlyObjects.GHMetaPackage.class,
                ReadOnlyObjects.GHMetaGettersUnmodifiable.class, ReadOnlyObjects.GHMetaGettersFinal.class,
                ReadOnlyObjects.GHMetaGettersFinalCreator.class, };

        for (Class metaClass : examples) {
            ReadOnlyObjects.GHMetaExample metaExample = gitHub.createRequest()
                    .withUrlPath("/meta")
                    .fetch((Class<ReadOnlyObjects.GHMetaExample>) metaClass);
            assertTrue(metaExample.isVerifiablePasswordAuthentication());
            assertEquals(19, metaExample.getApi().size());
            assertEquals(19, metaExample.getGit().size());
            assertEquals(3, metaExample.getHooks().size());
            assertEquals(6, metaExample.getImporter().size());
            assertEquals(6, metaExample.getPages().size());
            assertEquals(19, metaExample.getWeb().size());
        }
    }

    @Test
    public void getMyMarketplacePurchases() throws IOException {
        List<GHMarketplaceUserPurchase> userPurchases = gitHub.getMyMarketplacePurchases().toList();
        assertEquals(2, userPurchases.size());

        for (GHMarketplaceUserPurchase userPurchase : userPurchases) {
            assertFalse(userPurchase.isOnFreeTrial());
            assertNull(userPurchase.getFreeTrialEndsOn());
            assertEquals("monthly", userPurchase.getBillingCycle());

            GHMarketplacePlan plan = userPurchase.getPlan();
            // GHMarketplacePlan - Non-nullable fields
            assertNotNull(plan.getUrl());
            assertNotNull(plan.getAccountsUrl());
            assertNotNull(plan.getName());
            assertNotNull(plan.getDescription());
            assertNotNull(plan.getPriceModel());
            assertNotNull(plan.getState());

            // GHMarketplacePlan - primitive fields
            assertNotEquals(0L, plan.getId());
            assertNotEquals(0L, plan.getNumber());
            assertTrue(plan.getMonthlyPriceInCents() >= 0);

            // GHMarketplacePlan - list
            assertEquals(2, plan.getBullets().size());

            GHMarketplaceAccount account = userPurchase.getAccount();
            // GHMarketplaceAccount - Non-nullable fields
            assertNotNull(account.getLogin());
            assertNotNull(account.getUrl());
            assertNotNull(account.getType());

            // GHMarketplaceAccount - primitive fields
            assertNotEquals(0L, account.getId());

            /* logical combination tests */
            // Rationale: organization_billing_email is only set when account type is ORGANIZATION.
            if (account.getType() == ORGANIZATION)
                assertNotNull(account.getOrganizationBillingEmail());
            else
                assertNull(account.getOrganizationBillingEmail());
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

        // Header field names must be case-insensitive
        assertThat(org.getResponseHeaderFields().containsKey("CacHe-ContrOl"), is(true));

        // The KeySet from header fields should also be case-insensitive
        assertThat(org.getResponseHeaderFields().keySet().contains("CacHe-ControL"), is(true));
        assertThat(org.getResponseHeaderFields().keySet().contains("CacHe-ControL"), is(true));

        assertThat(org.getResponseHeaderFields().get("cachE-cOntrol").get(0), is("private, max-age=60, s-maxage=60"));

        // GitHub has started changing their headers to all lowercase.
        // For this test we want the field names to be with mixed-case (harder to do comparison).
        // Ensure that it remains that way, if test resources are ever refreshed.
        boolean found = false;
        for (String key : org.getResponseHeaderFields().keySet()) {
            if (Objects.equals("Cache-Control", key)) {
                found = true;
                break;
            }
        }
        assertThat("Must have the literal expected string 'Cache-Control' for header field name", found);
    }
}

package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kohsuke.github.GHDirection.DESC;
import static org.kohsuke.github.GHMarketplaceAccountType.ORGANIZATION;
import static org.kohsuke.github.GHMarketplaceListAccountBuilder.Sort.UPDATED;

/**
 * Tests for the GitHub MarketPlace Plan API methods
 */
public class GHMarketplacePlanTest extends AbstractGitHubWireMockTest {

    protected GitHubBuilder getGitHubBuilder() {
        return super.getGitHubBuilder()
                // ensure that only JWT will be used against the tests below
                .withPassword(null, null)
                .withJwtToken("bogus");
    }

    @Test
    public void listMarketplacePlans() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().asList();
        assertEquals(3, plans.size());
        plans.forEach(this::testMarketplacePlan);
    }

    @Test
    public void listAccounts() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().asList();
        assertEquals(3, plans.size());
        List<GHMarketplaceAccountPlan> marketplaceUsers = plans.get(0).listAccounts().createRequest().asList();
        assertEquals(2, marketplaceUsers.size());
        marketplaceUsers.forEach(this::testMarketplaceAccount);
    }

    @Test
    public void listAccountsWithDirection() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().asList();
        assertEquals(3, plans.size());

        for (GHMarketplacePlan plan : plans) {
            List<GHMarketplaceAccountPlan> marketplaceUsers = plan.listAccounts()
                    .direction(DESC)
                    .createRequest()
                    .asList();
            assertEquals(2, marketplaceUsers.size());
            marketplaceUsers.forEach(this::testMarketplaceAccount);
        }

    }

    @Test
    public void listAccountsWithSortAndDirection() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().asList();
        assertEquals(3, plans.size());

        for (GHMarketplacePlan plan : plans) {
            List<GHMarketplaceAccountPlan> marketplaceUsers = plan.listAccounts()
                    .sort(UPDATED)
                    .direction(DESC)
                    .createRequest()
                    .asList();
            assertEquals(2, marketplaceUsers.size());
            marketplaceUsers.forEach(this::testMarketplaceAccount);
        }

    }

    private void testMarketplacePlan(GHMarketplacePlan plan) {
        // Non-nullable fields
        assertNotNull(plan.getUrl());
        assertNotNull(plan.getAccountsUrl());
        assertNotNull(plan.getName());
        assertNotNull(plan.getDescription());
        assertNotNull(plan.getPriceModel());
        assertNotNull(plan.getState());

        // primitive fields
        assertNotEquals(0L, plan.getId());
        assertNotEquals(0L, plan.getNumber());
        assertTrue(plan.getMonthlyPriceInCents() >= 0);

        // list
        assertEquals(2, plan.getBullets().size());
    }

    private void testMarketplaceAccount(GHMarketplaceAccountPlan account) {
        // Non-nullable fields
        assertNotNull(account.getLogin());
        assertNotNull(account.getUrl());
        assertNotNull(account.getType());
        assertNotNull(account.getMarketplacePurchase());
        testMarketplacePurchase(account.getMarketplacePurchase());

        // primitive fields
        assertNotEquals(0L, account.getId());

        /* logical combination tests */
        // Rationale: organization_billing_email is only set when account type is ORGANIZATION.
        if (account.getType() == ORGANIZATION)
            assertNotNull(account.getOrganizationBillingEmail());
        else
            assertNull(account.getOrganizationBillingEmail());

        // Rationale: marketplace_pending_change isn't always set... This is what GitHub says about it:
        // "When someone submits a plan change that won't be processed until the end of their billing cycle,
        // you will also see the upcoming pending change."
        if (account.getMarketplacePendingChange() != null)
            testMarketplacePendingChange(account.getMarketplacePendingChange());
    }

    private void testMarketplacePurchase(GHMarketplacePurchase marketplacePurchase) {
        // Non-nullable fields
        assertNotNull(marketplacePurchase.getBillingCycle());
        assertNotNull(marketplacePurchase.getNextBillingDate());
        assertNotNull(marketplacePurchase.getUpdatedAt());
        testMarketplacePlan(marketplacePurchase.getPlan());

        /* logical combination tests */
        // Rationale: if onFreeTrial is true, then we should see free_trial_ends_on property set to something
        // different than null
        if (marketplacePurchase.isOnFreeTrial())
            assertNotNull(marketplacePurchase.getFreeTrialEndsOn());
        else
            assertNull(marketplacePurchase.getFreeTrialEndsOn());

        // Rationale: if price model is PER_UNIT then unit_count can't be null
        if (marketplacePurchase.getPlan().getPriceModel() == GHMarketplacePriceModel.PER_UNIT)
            assertNotNull(marketplacePurchase.getUnitCount());
        else
            assertNull(marketplacePurchase.getUnitCount());

    }

    private void testMarketplacePendingChange(GHMarketplacePendingChange marketplacePendingChange) {
        // Non-nullable fields
        assertNotNull(marketplacePendingChange.getEffectiveDate());
        testMarketplacePlan(marketplacePendingChange.getPlan());

        // primitive fields
        assertNotEquals(0L, marketplacePendingChange.getId());

        /* logical combination tests */
        // Rationale: if price model is PER_UNIT then unit_count can't be null
        if (marketplacePendingChange.getPlan().getPriceModel() == GHMarketplacePriceModel.PER_UNIT)
            assertNotNull(marketplacePendingChange.getUnitCount());
        else
            assertNull(marketplacePendingChange.getUnitCount());

    }

}

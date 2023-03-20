package org.kohsuke.github;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.kohsuke.github.GHDirection.DESC;
import static org.kohsuke.github.GHMarketplaceAccountType.ORGANIZATION;
import static org.kohsuke.github.GHMarketplaceListAccountBuilder.Sort.UPDATED;

// TODO: Auto-generated Javadoc
/**
 * Tests for the GitHub MarketPlace Plan API methods.
 *
 * @author Paulo Miguel Almeida
 */
public class GHMarketplacePlanTest extends AbstractGitHubWireMockTest {

    /**
     * Gets the git hub builder.
     *
     * @return the git hub builder
     */
    protected GitHubBuilder getGitHubBuilder() {
        return super.getGitHubBuilder()
                // ensure that only JWT will be used against the tests below
                .withPassword(null, null)
                .withJwtToken("bogus");
    }

    /**
     * List marketplace plans.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listMarketplacePlans() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().toList();
        assertThat(plans.size(), equalTo(3));
        plans.forEach(GHMarketplacePlanTest::testMarketplacePlan);
    }

    /**
     * List accounts.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listAccounts() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().toList();
        assertThat(plans.size(), equalTo(3));
        List<GHMarketplaceAccountPlan> marketplaceUsers = plans.get(0).listAccounts().createRequest().toList();
        assertThat(marketplaceUsers.size(), equalTo(2));
        marketplaceUsers.forEach(GHMarketplacePlanTest::testMarketplaceAccount);
    }

    /**
     * List accounts with direction.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listAccountsWithDirection() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().toList();
        assertThat(plans.size(), equalTo(3));

        for (GHMarketplacePlan plan : plans) {
            List<GHMarketplaceAccountPlan> marketplaceUsers = plan.listAccounts()
                    .direction(DESC)
                    .createRequest()
                    .toList();
            assertThat(marketplaceUsers.size(), equalTo(2));
            marketplaceUsers.forEach(GHMarketplacePlanTest::testMarketplaceAccount);
        }

    }

    /**
     * List accounts with sort and direction.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listAccountsWithSortAndDirection() throws IOException {
        List<GHMarketplacePlan> plans = gitHub.listMarketplacePlans().toList();
        assertThat(plans.size(), equalTo(3));

        for (GHMarketplacePlan plan : plans) {
            List<GHMarketplaceAccountPlan> marketplaceUsers = plan.listAccounts()
                    .sort(UPDATED)
                    .direction(DESC)
                    .createRequest()
                    .toList();
            assertThat(marketplaceUsers.size(), equalTo(2));
            marketplaceUsers.forEach(GHMarketplacePlanTest::testMarketplaceAccount);
        }

    }

    static void testMarketplacePlan(GHMarketplacePlan plan) {
        // Non-nullable fields
        assertThat(plan.getUrl(), notNullValue());
        assertThat(plan.getAccountsUrl(), notNullValue());
        assertThat(plan.getName(), notNullValue());
        assertThat(plan.getDescription(), notNullValue());
        assertThat(plan.getPriceModel(), notNullValue());
        assertThat(plan.getState(), notNullValue());

        // primitive fields
        assertThat(plan.getId(), not(0L));
        assertThat(plan.getNumber(), not(0L));
        assertThat(plan.getMonthlyPriceInCents(), greaterThanOrEqualTo(0L));

        // list
        assertThat(plan.getBullets().size(), Matchers.in(Arrays.asList(2, 3)));
    }

    static void testMarketplaceAccount(GHMarketplaceAccountPlan account) {
        // Non-nullable fields
        assertThat(account.getLogin(), notNullValue());
        assertThat(account.getUrl(), notNullValue());
        assertThat(account.getType(), notNullValue());
        assertThat(account.getMarketplacePurchase(), notNullValue());
        testMarketplacePurchase(account.getMarketplacePurchase());

        // primitive fields
        assertThat(account.getId(), not(0L));

        /* logical combination tests */
        // Rationale: organization_billing_email is only set when account type is ORGANIZATION.
        if (account.getType() == ORGANIZATION)
            assertThat(account.getOrganizationBillingEmail(), notNullValue());
        else
            assertThat(account.getOrganizationBillingEmail(), nullValue());

        // Rationale: marketplace_pending_change isn't always set... This is what GitHub says about it:
        // "When someone submits a plan change that won't be processed until the end of their billing cycle,
        // you will also see the upcoming pending change."
        if (account.getMarketplacePendingChange() != null)
            testMarketplacePendingChange(account.getMarketplacePendingChange());
    }

    static void testMarketplacePurchase(GHMarketplacePurchase marketplacePurchase) {
        // Non-nullable fields
        assertThat(marketplacePurchase.getBillingCycle(), notNullValue());
        assertThat(marketplacePurchase.getNextBillingDate(), notNullValue());
        assertThat(marketplacePurchase.getUpdatedAt(), notNullValue());
        testMarketplacePlan(marketplacePurchase.getPlan());

        /* logical combination tests */
        // Rationale: if onFreeTrial is true, then we should see free_trial_ends_on property set to something
        // different than null
        if (marketplacePurchase.isOnFreeTrial())
            assertThat(marketplacePurchase.getFreeTrialEndsOn(), notNullValue());
        else
            assertThat(marketplacePurchase.getFreeTrialEndsOn(), nullValue());

        // Rationale: if price model is PER_UNIT then unit_count can't be null
        if (marketplacePurchase.getPlan().getPriceModel() == GHMarketplacePriceModel.PER_UNIT)
            assertThat(marketplacePurchase.getUnitCount(), notNullValue());
        else
            assertThat(marketplacePurchase.getUnitCount(), Matchers.anyOf(nullValue(), is(1L)));

    }

    static void testMarketplacePendingChange(GHMarketplacePendingChange marketplacePendingChange) {
        // Non-nullable fields
        assertThat(marketplacePendingChange.getEffectiveDate(), notNullValue());
        testMarketplacePlan(marketplacePendingChange.getPlan());

        // primitive fields
        assertThat(marketplacePendingChange.getId(), not(0L));

        /* logical combination tests */
        // Rationale: if price model is PER_UNIT then unit_count can't be null
        if (marketplacePendingChange.getPlan().getPriceModel() == GHMarketplacePriceModel.PER_UNIT)
            assertThat(marketplacePendingChange.getUnitCount(), notNullValue());
        else
            assertThat(marketplacePendingChange.getUnitCount(), nullValue());

    }

}

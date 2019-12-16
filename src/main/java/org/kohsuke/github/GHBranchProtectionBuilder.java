package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.kohsuke.github.Previews.*;

/**
 * Builder to configure the branch protection settings.
 *
 * @see GHBranch#enableProtection() GHBranch#enableProtection()
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHBranchProtectionBuilder {
    private final GHBranch branch;

    private boolean enforceAdmins;
    private Map<String, Object> prReviews;
    private Restrictions restrictions;
    private StatusChecks statusChecks;

    GHBranchProtectionBuilder(GHBranch branch) {
        this.branch = branch;
    }

    /**
     * Add required checks gh branch protection builder.
     *
     * @param checks
     *            the checks
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder addRequiredChecks(Collection<String> checks) {
        getStatusChecks().contexts.addAll(checks);
        return this;
    }

    /**
     * Add required checks gh branch protection builder.
     *
     * @param checks
     *            the checks
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder addRequiredChecks(String... checks) {
        addRequiredChecks(Arrays.asList(checks));
        return this;
    }

    /**
     * Dismiss stale reviews gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder dismissStaleReviews() {
        return dismissStaleReviews(true);
    }

    /**
     * Dismiss stale reviews gh branch protection builder.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder dismissStaleReviews(boolean v) {
        getPrReviews().put("dismiss_stale_reviews", v);
        return this;
    }

    /**
     * Enable gh branch protection.
     *
     * @return the gh branch protection
     * @throws IOException
     *             the io exception
     */
    public GHBranchProtection enable() throws IOException {
        return requester().method("PUT")
                .withNullable("required_status_checks", statusChecks)
                .withNullable("required_pull_request_reviews", prReviews)
                .withNullable("restrictions", restrictions)
                .withNullable("enforce_admins", enforceAdmins)
                .withUrlPath(branch.getProtectionUrl().toString())
                .to(GHBranchProtection.class)
                .wrap(branch);
    }

    /**
     * Include admins gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder includeAdmins() {
        return includeAdmins(true);
    }

    /**
     * Include admins gh branch protection builder.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder includeAdmins(boolean v) {
        enforceAdmins = v;
        return this;
    }

    /**
     * Required reviewers gh branch protection builder.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requiredReviewers(int v) {
        getPrReviews().put("required_approving_review_count", v);
        return this;
    }

    /**
     * Require branch is up to date gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireBranchIsUpToDate() {
        return requireBranchIsUpToDate(true);
    }

    /**
     * Require branch is up to date gh branch protection builder.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireBranchIsUpToDate(boolean v) {
        getStatusChecks().strict = v;
        return this;
    }

    /**
     * Require code own reviews gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireCodeOwnReviews() {
        return requireCodeOwnReviews(true);
    }

    /**
     * Require code own reviews gh branch protection builder.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireCodeOwnReviews(boolean v) {
        getPrReviews().put("require_code_owner_reviews", v);
        return this;
    }

    /**
     * Require reviews gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireReviews() {
        getPrReviews();
        return this;
    }

    /**
     * Restrict review dismissals gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder restrictReviewDismissals() {
        getPrReviews();

        if (!prReviews.containsKey("dismissal_restrictions")) {
            prReviews.put("dismissal_restrictions", new Restrictions());
        }

        return this;
    }

    /**
     * Restrict push access gh branch protection builder.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder restrictPushAccess() {
        getRestrictions();
        return this;
    }

    /**
     * Team push access gh branch protection builder.
     *
     * @param teams
     *            the teams
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder teamPushAccess(Collection<GHTeam> teams) {
        for (GHTeam team : teams) {
            teamPushAccess(team);
        }
        return this;
    }

    /**
     * Team push access gh branch protection builder.
     *
     * @param teams
     *            the teams
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder teamPushAccess(GHTeam... teams) {
        for (GHTeam team : teams) {
            getRestrictions().teams.add(team.getSlug());
        }
        return this;
    }

    /**
     * Team review dismissals gh branch protection builder.
     *
     * @param teams
     *            the teams
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder teamReviewDismissals(Collection<GHTeam> teams) {
        for (GHTeam team : teams) {
            teamReviewDismissals(team);
        }
        return this;
    }

    /**
     * Team review dismissals gh branch protection builder.
     *
     * @param teams
     *            the teams
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder teamReviewDismissals(GHTeam... teams) {
        for (GHTeam team : teams) {
            addReviewRestriction(team.getSlug(), true);
        }
        return this;
    }

    /**
     * User push access gh branch protection builder.
     *
     * @param users
     *            the users
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder userPushAccess(Collection<GHUser> users) {
        for (GHUser user : users) {
            userPushAccess(user);
        }
        return this;
    }

    /**
     * User push access gh branch protection builder.
     *
     * @param users
     *            the users
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder userPushAccess(GHUser... users) {
        for (GHUser user : users) {
            getRestrictions().users.add(user.getLogin());
        }
        return this;
    }

    /**
     * User review dismissals gh branch protection builder.
     *
     * @param users
     *            the users
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder userReviewDismissals(Collection<GHUser> users) {
        for (GHUser team : users) {
            userReviewDismissals(team);
        }
        return this;
    }

    /**
     * User review dismissals gh branch protection builder.
     *
     * @param users
     *            the users
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder userReviewDismissals(GHUser... users) {
        for (GHUser user : users) {
            addReviewRestriction(user.getLogin(), false);
        }
        return this;
    }

    private void addReviewRestriction(String restriction, boolean isTeam) {
        restrictReviewDismissals();
        Restrictions restrictions = (Restrictions) prReviews.get("dismissal_restrictions");

        if (isTeam) {
            restrictions.teams.add(restriction);
        } else {
            restrictions.users.add(restriction);
        }
    }

    private Map<String, Object> getPrReviews() {
        if (prReviews == null) {
            prReviews = new HashMap<String, Object>();
        }
        return prReviews;
    }

    private Restrictions getRestrictions() {
        if (restrictions == null) {
            restrictions = new Restrictions();
        }
        return restrictions;
    }

    private StatusChecks getStatusChecks() {
        if (statusChecks == null) {
            statusChecks = new StatusChecks();
        }
        return statusChecks;
    }

    private Requester requester() {
        return branch.getRoot().retrieve().withPreview(LUKE_CAGE);
    }

    private static class Restrictions {
        private Set<String> teams = new HashSet<String>();
        private Set<String> users = new HashSet<String>();
    }

    private static class StatusChecks {
        final List<String> contexts = new ArrayList<String>();
        boolean strict;
    }
}

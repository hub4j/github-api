package org.kohsuke.github;

import static org.kohsuke.github.Previews.LOKI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
        "URF_UNREAD_FIELD" }, justification = "JSON API")
public class GHBranchProtectionBuilder {
    private final GHBranch branch;

    private boolean enforceAdmins;
    private Map<String, Object> prReviews;
    private Restrictions restrictions;
    private StatusChecks statusChecks;

    GHBranchProtectionBuilder(GHBranch branch) {
        this.branch = branch;
    }

    public GHBranchProtectionBuilder addRequiredChecks(Collection<String> checks) {
        getStatusChecks().contexts.addAll(checks);
        return this;
    }
    
    public GHBranchProtectionBuilder addRequiredChecks(String... checks) {
        addRequiredChecks(Arrays.asList(checks));
        return this;
    }

    public GHBranchProtectionBuilder dismissStateReviews() {
        getPrReviews().put("dismiss_stale_reviews", true);
        return this;
    }

    public GHBranchProtection enable() throws IOException {
        return requester().method("PUT")
                .withNullable("required_status_checks", statusChecks)
                .withNullable("required_pull_request_reviews", prReviews)
                .withNullable("restrictions", restrictions)
                .withNullable("enforce_admins", enforceAdmins)
                .to(branch.getProtectionUrl().toString(), GHBranchProtection.class);
    }

    public GHBranchProtectionBuilder includeAdmins() {
        enforceAdmins = true;
        return this;
    }
    
    public GHBranchProtectionBuilder requireBranchIsUpToDate() {
        getStatusChecks().strict = true;
        return this;
    }

    public GHBranchProtectionBuilder requireCodeOwnReviews() {
        getPrReviews().put("require_code_owner_reviews", true);
        return this;
    }

    public GHBranchProtectionBuilder requireReviews() {
        getPrReviews();
        return this;
    }

    public GHBranchProtectionBuilder restrictPushAccess() {
        getRestrictions();
        return this;
    }

    public GHBranchProtectionBuilder teamPushAccess(Collection<GHTeam> teams) {
        for (GHTeam team : teams) {
            teamPushAccess(team);
        }
        return this;
    }

    public GHBranchProtectionBuilder teamPushAccess(GHTeam... teams) {
        for (GHTeam team : teams) {
            getRestrictions().teams.add(team.getSlug());
        }
        return this;
    }

    public GHBranchProtectionBuilder teamReviewDismissals(Collection<GHTeam> teams) {
        for (GHTeam team : teams) {
            teamReviewDismissals(team);
        }
        return this;
    }

    public GHBranchProtectionBuilder teamReviewDismissals(GHTeam... teams) {
        for (GHTeam team : teams) {
            addReviewRestriction(team.getSlug(), true);
        }
        return this;
    }

    public GHBranchProtectionBuilder userPushAccess(Collection<GHUser> users) {
        for (GHUser user : users) {
            userPushAccess(user);
        }
        return this;
    }
    
    public GHBranchProtectionBuilder userPushAccess(GHUser... users) {
        for (GHUser user : users) {
            getRestrictions().users.add(user.getLogin());
        }
        return this;
    }

    public GHBranchProtectionBuilder userReviewDismissals(Collection<GHUser> users) {
        for (GHUser team : users) {
            userReviewDismissals(team);
        }
        return this;
    }

    public GHBranchProtectionBuilder userReviewDismissals(GHUser... users) {
        for (GHUser user : users) {
            addReviewRestriction(user.getLogin(), false);
        }
        return this;
    }

    private void addReviewRestriction(String restriction, boolean isTeam) {
        getPrReviews();
        
        if (!prReviews.containsKey("dismissal_restrictions")) {
            prReviews.put("dismissal_restrictions", new Restrictions());
        }
        
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
        return new Requester(branch.getRoot()).withPreview(LOKI);
    }

    private class Restrictions {
        private Set<String> teams = new HashSet<String>();
        private Set<String> users = new HashSet<String>();
    }

    private class StatusChecks {
        final List<String> contexts = new ArrayList<String>();
        boolean strict;
    }
}

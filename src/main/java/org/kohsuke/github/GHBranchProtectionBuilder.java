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

import static org.kohsuke.github.internal.Previews.LUKE_CAGE;

// TODO: Auto-generated Javadoc
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

    private Map<String, Object> fields = new HashMap<String, Object>();
    private Map<String, Object> prReviews;
    private Restrictions restrictions;
    private StatusChecks statusChecks;

    /**
     * Instantiates a new GH branch protection builder.
     *
     * @param branch
     *            the branch
     */
    GHBranchProtectionBuilder(GHBranch branch) {
        this.branch = branch;
        includeAdmins(false);
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
     * Allow deletion of the protected branch.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowDeletions() {
        return allowDeletions(true);
    }

    /**
     * Allows deletion of the protected branch by anyone with write access to the repository.
     * Set to true to allow deletion of the protected branch. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowDeletions(boolean v) {
        fields.put("allow_deletions", v);
        return this;
    }

    /**
     * Permits force pushes.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowForcePushes() {
        return allowForcePushes(true);
    }

    /**
     * Permits force pushes to the protected branch by anyone with write access to the repository.
     * Set to true to allow force pushes. Set to false to block force pushes. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowForcePushes(boolean v) {
        fields.put("allow_force_pushes", v);
        return this;
    }

    /**
     * Allow fork syncing.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowForkSyncing() {
        return allowForkSyncing(true);
    }

    /**
     * Whether users can pull changes from upstream when the branch is locked. Set to true to allow fork syncing.
     * Set to true to enable. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder allowForkSyncing(boolean v) {
        fields.put("allow_fork_syncing", v);
        return this;
    }

    /**
     * Restrict new branch creation.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder blockCreations() {
        return blockCreations(true);
    }

    /**
     * If set to true, the restrictions branch protection settings which limits who can push will also block pushes
     * which create new branches, unless the push is initiated by a user, team, or app which has the ability to push.
     * Set to true to restrict new branch creation. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder blockCreations(boolean v) {
        fields.put("block_creations", v);
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
                .with(fields)
                .withNullable("required_status_checks", statusChecks)
                .withNullable("required_pull_request_reviews", prReviews)
                .withNullable("restrictions", restrictions)
                .withUrlPath(branch.getProtectionUrl().toString())
                .fetch(GHBranchProtection.class);
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
        fields.put("enforce_admins", v);
        return this;
    }

    /**
     * Set the branch as read-only.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder lockBranch() {
        return lockBranch(true);
    }

    /**
     * Whether to set the branch as read-only. If this is true, users will not be able to push to the branch.
     * Set to true to enable. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder lockBranch(boolean v) {
        fields.put("lock_branch", v);
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
     * Enable the most recent push must be approved by someone other than the person who pushed it.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireLastPushApproval() {
        return requireLastPushApproval(true);
    }

    /**
     * Whether the most recent push must be approved by someone other than the person who pushed it. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requireLastPushApproval(boolean v) {
        getPrReviews().put("require_last_push_approval", v);
        return this;
    }

    /**
     * Require all conversations on code to be resolved before a pull request can be merged into a branch that
     * matches this rule.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requiredConversationResolution() {
        return requiredConversationResolution(true);
    }

    /**
     * Require all conversations on code to be resolved before a pull request can be merged into a branch that
     * matches this rule.
     * Set to true to enable. Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requiredConversationResolution(boolean v) {
        fields.put("required_conversation_resolution", v);
        return this;
    }

    /**
     * Enforce a linear commit Git history.
     *
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requiredLinearHistory() {
        return requiredLinearHistory(true);
    }

    /**
     * Enforces a linear commit Git history, which prevents anyone from pushing merge commits to a branch. Set to true
     * to enforce a linear commit history. Set to false to disable a linear commit Git history. Your repository must
     * allow squash merging or rebase merging before you can enable a linear commit history.
     * Default: false.
     *
     * @param v
     *            the v
     * @return the gh branch protection builder
     */
    public GHBranchProtectionBuilder requiredLinearHistory(boolean v) {
        fields.put("required_linear_history", v);
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
        return branch.root().createRequest().withPreview(LUKE_CAGE);
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

package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.kohsuke.github.internal.Previews.ZZZAX;

// TODO: Auto-generated Javadoc
/**
 * The type GHBranchProtection.
 *
 * @see <a href="https://docs.github.com/en/rest/reference/repos#get-branch-protection">GitHub Branch Protection</a>
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHBranchProtection extends GitHubInteractiveObject {
    private static final String REQUIRE_SIGNATURES_URI = "/required_signatures";

    @JsonProperty
    private AllowDeletions allowDeletions;

    @JsonProperty
    private AllowForcePushes allowForcePushes;

    @JsonProperty
    private AllowForkSyncing allowForkSyncing;

    @JsonProperty
    private BlockCreations blockCreations;

    @JsonProperty
    private EnforceAdmins enforceAdmins;

    @JsonProperty
    private LockBranch lockBranch;

    @JsonProperty
    private RequiredConversationResolution requiredConversationResolution;

    @JsonProperty
    private RequiredLinearHistory requiredLinearHistory;

    @JsonProperty("required_pull_request_reviews")
    private RequiredReviews requiredReviews;

    @JsonProperty
    private RequiredStatusChecks requiredStatusChecks;

    @JsonProperty
    private Restrictions restrictions;

    @JsonProperty
    private String url;

    /**
     * Enabled signed commits.
     *
     * @throws IOException
     *             the io exception
     */
    @Preview(ZZZAX)
    public void enabledSignedCommits() throws IOException {
        requester().method("POST").withUrlPath(url + REQUIRE_SIGNATURES_URI).fetch(RequiredSignatures.class);
    }

    /**
     * Disable signed commits.
     *
     * @throws IOException
     *             the io exception
     */
    @Preview(ZZZAX)
    public void disableSignedCommits() throws IOException {
        requester().method("DELETE").withUrlPath(url + REQUIRE_SIGNATURES_URI).send();
    }

    /**
     * Gets allow deletions.
     *
     * @return the enforce admins
     */
    public AllowDeletions getAllowDeletions() {
        return allowDeletions;
    }

    /**
     * Gets allow force pushes.
     *
     * @return the enforce admins
     */
    public AllowForcePushes getAllowForcePushes() {
        return allowForcePushes;
    }

    /**
     * Gets allow fork syncing.
     *
     * @return the enforce admins
     */
    public AllowForkSyncing getAllowForkSyncing() {
        return allowForkSyncing;
    }

    /**
     * Gets block creations.
     *
     * @return the enforce admins
     */
    public BlockCreations getBlockCreations() {
        return blockCreations;
    }

    /**
     * Gets enforce admins.
     *
     * @return the enforce admins
     */
    public EnforceAdmins getEnforceAdmins() {
        return enforceAdmins;
    }

    /**
     * Gets lock branch.
     *
     * @return the enforce admins
     */
    public LockBranch getLockBranch() {
        return lockBranch;
    }

    /**
     * Gets required conversation resolution.
     *
     * @return the enforce admins
     */
    public RequiredConversationResolution getRequiredConversationResolution() {
        return requiredConversationResolution;
    }

    /**
     * Gets required linear history.
     *
     * @return the enforce admins
     */
    public RequiredLinearHistory getRequiredLinearHistory() {
        return requiredLinearHistory;
    }

    /**
     * Gets required reviews.
     *
     * @return the required reviews
     */
    public RequiredReviews getRequiredReviews() {
        return requiredReviews;
    }

    /**
     * Gets required signatures.
     *
     * @return the required signatures
     * @throws IOException
     *             the io exception
     */
    @Preview(ZZZAX)
    public boolean getRequiredSignatures() throws IOException {
        return requester().withUrlPath(url + REQUIRE_SIGNATURES_URI).fetch(RequiredSignatures.class).enabled;
    }

    /**
     * Gets required status checks.
     *
     * @return the required status checks
     */
    public RequiredStatusChecks getRequiredStatusChecks() {
        return requiredStatusChecks;
    }

    /**
     * Gets restrictions.
     *
     * @return the restrictions
     */
    public Restrictions getRestrictions() {
        return restrictions;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    private Requester requester() {
        return root().createRequest().withPreview(ZZZAX);
    }

    /**
     * The type AllowDeletions.
     */
    public static class AllowDeletions {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type AllowForcePushes.
     */
    public static class AllowForcePushes {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type AllowForkSyncing.
     */
    public static class AllowForkSyncing {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type BlockCreations.
     */
    public static class BlockCreations {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type EnforceAdmins.
     */
    public static class EnforceAdmins {
        @JsonProperty
        private boolean enabled;

        @JsonProperty
        private String url;

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type LockBranch.
     */
    public static class LockBranch {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type RequiredConversationResolution.
     */
    public static class RequiredConversationResolution {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type RequiredLinearHistory.
     */
    public static class RequiredLinearHistory {
        @JsonProperty
        private boolean enabled;

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type RequiredReviews.
     */
    public static class RequiredReviews {
        @JsonProperty("dismissal_restrictions")
        private Restrictions dismissalRestriction;

        @JsonProperty
        private boolean dismissStaleReviews;

        @JsonProperty
        private boolean requireCodeOwnerReviews;

        @JsonProperty
        private boolean requireLastPushApproval;

        @JsonProperty("required_approving_review_count")
        private int requiredReviewers;

        @JsonProperty
        private String url;

        /**
         * Gets dismissal restrictions.
         *
         * @return the dismissal restrictions
         */
        public Restrictions getDismissalRestrictions() {
            return dismissalRestriction;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Is dismiss stale reviews boolean.
         *
         * @return the boolean
         */
        public boolean isDismissStaleReviews() {
            return dismissStaleReviews;
        }

        /**
         * Is require code owner reviews boolean.
         *
         * @return the boolean
         */
        public boolean isRequireCodeOwnerReviews() {
            return requireCodeOwnerReviews;
        }

        /**
         * Is require last push approval boolean.
         *
         * @return the boolean
         */
        public boolean isRequireLastPushApproval() {
            return requireLastPushApproval;
        }

        /**
         * Gets required reviewers.
         *
         * @return the required reviewers
         */
        public int getRequiredReviewers() {
            return requiredReviewers;
        }
    }

    private static class RequiredSignatures {
        @JsonProperty
        private boolean enabled;

        @JsonProperty
        private String url;

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Is enabled boolean.
         *
         * @return the boolean
         */
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * The type RequiredStatusChecks.
     */
    public static class RequiredStatusChecks {
        @JsonProperty
        private Collection<String> contexts;

        @JsonProperty
        private boolean strict;

        @JsonProperty
        private String url;

        /**
         * Gets contexts.
         *
         * @return the contexts
         */
        public Collection<String> getContexts() {
            return Collections.unmodifiableCollection(contexts);
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Is requires branch up to date boolean.
         *
         * @return the boolean
         */
        public boolean isRequiresBranchUpToDate() {
            return strict;
        }
    }

    /**
     * The type Restrictions.
     */
    public static class Restrictions {
        @JsonProperty
        private Collection<GHTeam> teams;

        private String teamsUrl;

        @JsonProperty
        private String url;

        @JsonProperty
        private Collection<GHUser> users;

        private String usersUrl;

        /**
         * Gets teams.
         *
         * @return the teams
         */
        public Collection<GHTeam> getTeams() {
            return Collections.unmodifiableCollection(teams);
        }

        /**
         * Gets teams url.
         *
         * @return the teams url
         */
        public String getTeamsUrl() {
            return teamsUrl;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gets users.
         *
         * @return the users
         */
        public Collection<GHUser> getUsers() {
            return Collections.unmodifiableCollection(users);
        }

        /**
         * Gets users url.
         *
         * @return the users url
         */
        public String getUsersUrl() {
            return usersUrl;
        }
    }
}

package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.kohsuke.github.Previews.ZZZAX;

import java.io.IOException;
import java.util.Collection;

@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
        "URF_UNREAD_FIELD"}, justification = "JSON API")
public class GHBranchProtection extends GHObjectBase {
    private static final String REQUIRE_SIGNATURES_URI = "/required_signatures";

    @JsonProperty("enforce_admins")
    private EnforceAdmins enforceAdmins;


    @JsonProperty("required_pull_request_reviews")
    private RequiredReviews requiredReviews;

    @JsonProperty("required_status_checks")
    private RequiredStatusChecks requiredStatusChecks;

    @JsonProperty
    private Restrictions restrictions;

    @JsonProperty
    private String url;

    @Preview @Deprecated
    public void enabledSignedCommits() throws IOException {
        requester().method("POST")
                .to(url + REQUIRE_SIGNATURES_URI, RequiredSignatures.class);
    }

    @Preview @Deprecated
    public void disableSignedCommits() throws IOException {
        requester().method("DELETE")
                .to(url + REQUIRE_SIGNATURES_URI);
    }

    public EnforceAdmins getEnforceAdmins() {
        return enforceAdmins;
    }

    public RequiredReviews getRequiredReviews() {
        return requiredReviews;
    }

    @Preview @Deprecated
    public boolean getRequiredSignatures() throws IOException {
        return requester().method("GET")
                .to(url + REQUIRE_SIGNATURES_URI, RequiredSignatures.class).enabled;
    }

    public RequiredStatusChecks getRequiredStatusChecks() {
        return requiredStatusChecks;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public String getUrl() {
        return url;
    }

    GHBranchProtection wrap(GHBranch branch) {
        return this;
    }

    private Requester requester() {
        return createRequest().withPreview(ZZZAX);
    }

    public static class EnforceAdmins {
        @JsonProperty
        private boolean enabled;

        @JsonProperty
        private String url;

        public String getUrl() {
            return url;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class RequiredReviews {
        @JsonProperty("dismissal_restrictions")
        private Restrictions dismissalRestriction;

        @JsonProperty("dismiss_stale_reviews")
        private boolean dismissStaleReviews;

        @JsonProperty("require_code_owner_reviews")
        private boolean requireCodeOwnerReviews;

        @JsonProperty("required_approving_review_count")
        private int requiredReviewers;

        @JsonProperty
        private String url;

        public Restrictions getDismissalRestrictions() {
            return dismissalRestriction;
        }

        public String getUrl() {
            return url;
        }

        public boolean isDismissStaleReviews() {
            return dismissStaleReviews;
        }

        public boolean isRequireCodeOwnerReviews() {
            return requireCodeOwnerReviews;
        }

        public int getRequiredReviewers() {
            return requiredReviewers;
        }
    }

    private static class RequiredSignatures {
        @JsonProperty
        private boolean enabled;

        @JsonProperty
        private String url;

        public String getUrl() {
            return url;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class RequiredStatusChecks {
        @JsonProperty
        private Collection<String> contexts;

        @JsonProperty
        private boolean strict;

        @JsonProperty
        private String url;

        public Collection<String> getContexts() {
            return contexts;
        }

        public String getUrl() {
            return url;
        }

        public boolean isRequiresBranchUpToDate() {
            return strict;
        }
    }

    public static class Restrictions {
        @JsonProperty
        private Collection<GHTeam> teams;

        @JsonProperty("teams_url")
        private String teamsUrl;

        @JsonProperty
        private String url;

        @JsonProperty
        private Collection<GHUser> users;

        @JsonProperty("users_url")
        private String usersUrl;

        public Collection<GHTeam> getTeams() {
            return teams;
        }

        public String getTeamsUrl() {
            return teamsUrl;
        }

        public String getUrl() {
            return url;
        }

        public Collection<GHUser> getUsers() {
            return users;
        }

        public String getUsersUrl() {
            return usersUrl;
        }
    }
}

package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Code scanning alert for a repository
 *
 * <a href="https://docs.github.com/en/rest/reference/code-scanning"></a>
 */
@SuppressFBWarnings(value = { "UUF_UNUSED_FIELD" }, justification = "JSON API")
public class GHCodeScanningAlert extends GHObject {
    @JsonIgnore
    private GHRepository owner;
    private long number;
    private String html_url;
    private GHCodeScanningAlertState state;
    private GHUser dismissed_by;
    private String dismissed_at;
    private String dismissed_reason;
    private Tool tool;
    private Rule rule;
    private GHCodeScanningAlertInstance most_recent_instance;
    private String instances_url;

    GHCodeScanningAlert wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Id/number of the alert.
     *
     * @return the id/number
     * @see #getId()
     */
    public long getNumber() {
        return number;
    }

    /**
     * Id/number of the alert.
     *
     * @return the id/number
     * @see #getNumber()
     */
    @Override
    public long getId() {
        return getNumber();
    }

    /**
     * State of alert
     *
     * @return the state
     */
    public GHCodeScanningAlertState getState() {
        return state;
    }

    /**
     * User that has dismissed the alert. Non-null when {@link #getState()} is <i>Dismissed</i>
     *
     * <p>
     * Note: User object returned by code scanning GitHub API does not contain all fields. Use with caution
     * </p>
     *
     * @return the user
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getDismissedBy() {
        return dismissed_by;
    }

    /**
     * Time when alert was dismissed. Non-null when {@link #getState()} is <i>Dismissed</i>
     *
     * @return the time
     */
    public Date getDismissedAt() {
        return GitHubClient.parseDate(dismissed_at);
    }

    /**
     * Reason provided for dismissing the alert.
     *
     * @return the reason
     */
    public String getDismissedReason() {
        return dismissed_reason;
    }

    /**
     * Code scanning tool that created this alert
     *
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Code scanning rule that was violated, causing the alert
     *
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Most recent instance of the alert
     *
     * @return most recent instance
     */
    public GHCodeScanningAlertInstance getMostRecentInstance() {
        return most_recent_instance;
    }

    /**
     * List all instances of the alert
     *
     * @return the paged iterable
     */
    public PagedIterable<GHCodeScanningAlertInstance> listAlertInstances() {
        return new GHCodeScanningAlertInstancesIterable(this,
                root().createRequest().withUrlPath(instances_url).build());
    }

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Code scanning rule
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    static class Rule {
        private String id;
        private String name;
        private String description;
        private String severity;
        private String security_severity_level;
        private String[] tags;
        private String full_description;
        private String help;

        private String help_uri;

        /**
         * A unique identifier for the rule used to detect the alert.
         *
         * @return the id
         */
        @Nullable
        public String getId() {
            return id;
        }

        /**
         * The name of the rule used to detect the alert.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * The severity of the alert.
         *
         * @return the severity
         */
        @Nullable
        public String getSeverity() {
            return severity;
        }

        /**
         * The security severity of the alert.
         *
         * @return the security severity
         */
        @Nullable
        public String getSecuritySeverityLevel() {
            return security_severity_level;
        }

        /**
         * A short description of the rule used to detect the alert.
         *
         * @return the description
         */
        @Nonnull
        public String getDescription() {
            return description;
        }

        /**
         * A set of tags applicable for the rule.
         *
         * @return the tags
         */
        @Nullable
        public String[] getTags() {
            return tags;
        }

        // The following fields only appear on some endpoints.
        // These might be empty on endpoints like listSecurityAlerts

        /**
         * Full description of rule
         *
         * @return the full description
         */
        @Nonnull
        public String getFullDescription() {
            return full_description;
        }

        /**
         * Help text for the rule
         *
         * @return the help text
         */
        @Nullable
        public String getHelp() {
            return help;
        }

        /**
         * A link to documentation for the rule used to detect the alert. Can be null.
         *
         * @return alert documentation url
         */
        @Nullable
        public String getHelpUri() {
            return help_uri;
        }
    }

    /**
     * Code scanning tool
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    static class Tool {
        private String name;
        private String guid;
        private String version;

        /**
         * Name of code scanning tool
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * GUID of code scanning tool
         *
         * @return the GUID
         */
        public String getGuid() {
            return guid;
        }

        /**
         * Version of code scanning tool
         *
         * @return the version
         */
        public String getVersion() {
            return version;
        }
    }
}

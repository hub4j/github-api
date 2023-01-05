package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

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
     * Severity of the code scanning rule that was violated
     * @return the severity
     */
    public String getSeverity() { return rule.severity; }

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
        private String severity;
        private String description;
        private String name;
        private String full_description;
        private String[] tags;
        private String help;

        /**
         * Id of rule
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Severity of rule
         *
         * @return the severity
         */
        public String getSeverity() {
            return severity;
        }

        /**
         * Description of rule
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Name of rule
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Full description of rule
         *
         * @return the full description
         */
        public String getFullDescription() {
            return full_description;
        }

        /**
         * Tags associated with the rule
         *
         * @return the tags
         */
        public String[] getTags() {
            return tags;
        }

        /**
         * Help text for the rule
         *
         * @return the help text
         */
        public String getHelp() {
            return help;
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

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents an issue type configured for a GitHub organization.
 *
 * @see GHOrganization#listIssueTypes() GHOrganization#listIssueTypes()
 * @see <a href="https://docs.github.com/en/rest/orgs/issue-types">Issue types API</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHIssueType extends GHObject {

    private String color;
    private String description;
    private boolean isEnabled;
    private String name;

    /**
     * Create default GHIssueType instance.
     */
    public GHIssueType() {
    }

    /**
     * Gets the issue type color.
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Gets the issue type description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the issue type name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the issue type is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
}

package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The type GHIssueRename.
 *
 * @author Andrii Tomchuk
 * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#renamed">Github
 *      documentation for renamed event</a>
 */
public class GHIssueRename {
    private String from = "";
    private String to = "";

    /**
     * Old issue name.
     *
     * @return old issue name
     */
    public String getFrom() {
        return this.from;
    }

    /**
     * New issue name.
     *
     * @return new issue name
     */
    public String getTo() {
        return this.to;
    }
}

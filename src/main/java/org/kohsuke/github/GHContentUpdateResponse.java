package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * The response that is returned when updating repository content.
 */
public class GHContentUpdateResponse {

    private GitCommit commit;

    private GHContent content;
    /**
     * Create default GHContentUpdateResponse instance
     */
    public GHContentUpdateResponse() {
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GitCommit getCommit() {
        return commit;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHContent getContent() {
        return content;
    }

}

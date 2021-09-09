package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The response that is returned when updating repository content.
 */
public class GHContentUpdateResponse {
    private GHContent content;
    private GHCommit commit;

    /**
     * Gets content.
     *
     * @return the content
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHContent getContent() {
        return content;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHCommit getCommit() {
        return commit;
    }
}

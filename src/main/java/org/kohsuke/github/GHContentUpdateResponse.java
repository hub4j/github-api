package org.kohsuke.github;

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
    public GHContent getContent() {
        return content;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    public GHCommit getCommit() {
        return commit;
    }
}

package org.kohsuke.github;

/**
 * The response that is returned when updating
 * repository content.
**/
public class GHContentUpdateResponse {
    private GHContent content;
    private GHCommit commit;

    public GHContent getContent() {
        return content;
    }

    public GHCommit getCommit() {
        return commit;
    }
}

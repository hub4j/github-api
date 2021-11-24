package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wrapper for changes on issue and pull request review comments action="edited"
 *
 * @see GHEventPayload.IssueComment
 * @see GHEventPayload.PullRequestReviewComment
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "JSON API")
public class GHCommentChanges {

    private GHFrom body;

    /**
     * Gets the previous comment body.
     *
     * @return previous comment body (or null if not changed)
     */
    public GHFrom getBody() {
        return body;
    }

    /**
     * Wrapper for changed values.
     */
    public static class GHFrom {
        private String from;

        /**
         * Previous comment value that was changed.
         *
         * @return previous value
         */
        public String getFrom() {
            return from;
        }
    }
}

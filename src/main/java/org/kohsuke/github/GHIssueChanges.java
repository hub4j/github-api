package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Wrapper to define changed fields on issues action="edited".
 *
 * @see GHEventPayload.Issue
 */
@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public class GHIssueChanges {

    /**
     * Wrapper for changed values.
     */
    public static class GHFrom {

        private String from;

        /**
         * Create default GHFrom instance
         */
        public GHFrom() {
        }

        /**
         * Previous value that was changed.
         *
         * @return previous value
         */
        public String getFrom() {
            return from;
        }
    }

    private GHFrom body;
    private GHFrom title;

    /**
     * Create default GHIssueChanges instance
     */
    public GHIssueChanges() {
    }

    /**
     * Old issue body.
     *
     * @return old issue body (or null if not changed)
     */
    public GHFrom getBody() {
        return body;
    }

    /**
     * Old issue title.
     *
     * @return old issue title (or null if not changed)
     */
    public GHFrom getTitle() {
        return title;
    }
}

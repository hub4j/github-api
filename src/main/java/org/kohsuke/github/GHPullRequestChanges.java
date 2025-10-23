package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Wrapper to define changed fields on pull_request action="edited".
 *
 * @see GHEventPayload.PullRequest
 */
@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public class GHPullRequestChanges {

    /**
     * The Class GHCommitPointer.
     *
     * @see org.kohsuke.github.GHCommitPointer
     */
    public static class GHCommitPointer {

        private GHFrom ref;

        private GHFrom sha;
        /**
         * Create default GHCommitPointer instance
         */
        public GHCommitPointer() {
        }

        /**
         * Named ref to the commit. This (from value) appears to be a "short ref" that doesn't include "refs/heads/"
         * portion.
         *
         * @return the ref
         */
        public GHFrom getRef() {
            return ref;
        }

        /**
         * SHA1 of the commit.
         *
         * @return sha
         */
        public GHFrom getSha() {
            return sha;
        }
    }

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
    private GHCommitPointer base;
    private GHFrom body;

    private GHFrom title;

    /**
     * Create default GHPullRequestChanges instance
     */
    public GHPullRequestChanges() {
    }

    /**
     * Old target branch for pull request.
     *
     * @return old target branch info (or null if not changed)
     */
    public GHCommitPointer getBase() {
        return base;
    }

    /**
     * Old pull request body.
     *
     * @return old pull request body (or null if not changed)
     */
    public GHFrom getBody() {
        return body;
    }

    /**
     * Old pull request title.
     *
     * @return old pull request title (or null if not changed)
     */
    public GHFrom getTitle() {
        return title;
    }
}

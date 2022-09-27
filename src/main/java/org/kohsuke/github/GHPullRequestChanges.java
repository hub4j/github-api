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

    private GHCommitPointer base;
    private GHFrom title;
    private GHFrom body;

    /**
     * Old target branch for pull request.
     *
     * @return old target branch info (or null if not changed)
     */
    public GHCommitPointer getBase() {
        return base;
    }

    /**
     * Old pull request title.
     *
     * @return old pull request title (or null if not changed)
     */
    public GHFrom getTitle() {
        return title;
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
     * The Class GHCommitPointer.
     *
     * @see org.kohsuke.github.GHCommitPointer
     */
    public static class GHCommitPointer {
        private GHFrom ref;
        private GHFrom sha;

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
         * Previous value that was changed.
         *
         * @return previous value
         */
        public String getFrom() {
            return from;
        }
    }
}

package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Current state of {@link GHPullRequestReview}.
 */
public enum GHPullRequestReviewState {

    /** The pending. */
    PENDING,

    /** The approved. */
    APPROVED,

    /** The changes requested. */
    CHANGES_REQUESTED,

    /** The commented. */
    COMMENTED,

    /** The dismissed. */
    DISMISSED;

    /**
     * Action string.
     *
     * @return the string
     */
    String action() {
        GHPullRequestReviewEvent e = toEvent();
        return e == null ? null : e.action();
    }

    /**
     * To event.
     *
     * @return the GH pull request review event
     */
    GHPullRequestReviewEvent toEvent() {
        switch (this) {
            case PENDING :
                return GHPullRequestReviewEvent.PENDING;
            case APPROVED :
                return GHPullRequestReviewEvent.APPROVE;
            case CHANGES_REQUESTED :
                return GHPullRequestReviewEvent.REQUEST_CHANGES;
            case COMMENTED :
                return GHPullRequestReviewEvent.COMMENT;
        }
        return null;
    }
}

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

    /**
     * The request changes.
     *
     * @deprecated This was the thing when this API was in preview, but it changed when it became public. Use
     *             {@link #CHANGES_REQUESTED}. Left here for compatibility.
     */
    REQUEST_CHANGES,

    /** The commented. */
    COMMENTED,

    /** The dismissed. */
    DISMISSED;

    /**
     * Action string.
     *
     * @return the string
     * @deprecated This was an internal method accidentally exposed. Left here for compatibility.
     */
    public String action() {
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
            case REQUEST_CHANGES :
                return GHPullRequestReviewEvent.REQUEST_CHANGES;
            case COMMENTED :
                return GHPullRequestReviewEvent.COMMENT;
        }
        return null;
    }
}

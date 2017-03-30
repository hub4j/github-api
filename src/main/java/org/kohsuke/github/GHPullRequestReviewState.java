package org.kohsuke.github;

public enum GHPullRequestReviewState {
    PENDING(null),
    APPROVED("APPROVE"),
    REQUEST_CHANGES("REQUEST_CHANGES"),
    COMMENTED("COMMENT"),
    DISMISSED(null);

    private final String _action;

    GHPullRequestReviewState(String action) {
        _action = action;
    }

    public String action() {
        return _action;
    }
}

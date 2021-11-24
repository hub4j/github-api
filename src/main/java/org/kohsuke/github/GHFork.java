package org.kohsuke.github;

/**
 * The enum for Fork search mode
 */
public enum GHFork {

    /**
     * Search in the parent repository and in forks with more stars than the parent repository.
     *
     * Forks with the same or fewer stars than the parent repository are still ignored.
     */
    PARENT_AND_FORKS("true"),

    /**
     * Search only in forks with more stars than the parent repository.
     *
     * The parent repository is ignored. If no forks have more stars than the parent, no results will be returned.
     */
    FORKS_ONLY("only"),

    /**
     * (Default) Search only the parent repository.
     *
     * Forks are ignored.
     */
    PARENT_ONLY("");

    private String filterMode;
    GHFork(final String mode) {
        this.filterMode = mode;
    }

    @Override
    public String toString() {
        return filterMode;
    }
}

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * A stargazer at a repository on GitHub.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHStargazer {

    private GHRepository repository;
    private String starred_at;
    private GHUser user;

    /**
     * Gets the repository that is stargazed
     *
     * @return the starred repository
     */
    public GHRepository getRepository() {
        return repository;
    }

    /**
     * Gets the date when the repository was starred, however old stars before August 2012, will all show the date the
     * API was changed to support starred_at.
     *
     * @return the date the stargazer was added
     */
    public Date getStarredAt() {
        return GitHubClient.parseDate(starred_at);
    }

    /**
     * Gets the user that starred the repository
     *
     * @return the stargazer user
     */
    public GHUser getUser() {
        return user;
    }

    void wrapUp(GHRepository repository) {
        this.repository = repository;
        user.wrapUp(repository.root);
    }
}

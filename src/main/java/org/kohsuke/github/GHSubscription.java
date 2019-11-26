package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Represents your subscribing to a repository / conversation thread..
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getSubscription() GHRepository#getSubscription()
 * @see GHThread#getSubscription() GHThread#getSubscription()
 */
public class GHSubscription {
    private String created_at, url, repository_url, reason;
    private boolean subscribed, ignored;

    private GitHub root;
    private GHRepository repo;

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets repository url.
     *
     * @return the repository url
     */
    public String getRepositoryUrl() {
        return repository_url;
    }

    /**
     * Gets reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Is subscribed boolean.
     *
     * @return the boolean
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Is ignored boolean.
     *
     * @return the boolean
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * Gets repository.
     *
     * @return the repository
     */
    public GHRepository getRepository() {
        return repo;
    }

    /**
     * Removes this subscription.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").withUrlPath(repo.getApiTailUrl("subscription")).to();
    }

    GHSubscription wrapUp(GHRepository repo) {
        this.repo = repo;
        return wrapUp(repo.root);
    }

    GHSubscription wrapUp(GitHub root) {
        this.root = root;
        return this;
    }
}

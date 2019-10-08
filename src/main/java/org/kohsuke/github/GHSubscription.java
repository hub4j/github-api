package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Represents your subscribing to a repository / conversation thread..
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getSubscription()
 * @see GHThread#getSubscription()
 */
public class GHSubscription {
    private String created_at, url, repository_url, reason;
    private boolean subscribed, ignored;

    private GitHub root;
    private GHRepository repo;

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public String getUrl() {
        return url;
    }

    public String getRepositoryUrl() {
        return repository_url;
    }

    public String getReason() {
        return reason;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public GHRepository getRepository() {
        return repo;
    }

    /**
     * Removes this subscription.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(repo.getApiTailUrl("subscription"));
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

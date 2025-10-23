package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * Represents your subscribing to a repository / conversation thread..
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getSubscription() GHRepository#getSubscription()
 * @see GHThread#getSubscription() GHThread#getSubscription()
 */
public class GHSubscription extends GitHubInteractiveObject {

    private String created_at, url, repository_url, reason;

    private GHRepository repo;
    private boolean subscribed, ignored;

    /**
     * Create default GHSubscription instance
     */
    public GHSubscription() {
    }

    /**
     * Removes this subscription.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(repo.getApiTailUrl("subscription")).send();
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return GitHubClient.parseDate(created_at);
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
     * Gets repository.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return repo;
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
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
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
     * Is subscribed boolean.
     *
     * @return the boolean
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Wrap up.
     *
     * @param repo
     *            the repo
     * @return the GH subscription
     */
    GHSubscription wrapUp(GHRepository repo) {
        this.repo = repo;
        return this;
    }
}

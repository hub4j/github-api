package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.time.Instant;
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

    /**
     * Create default GHSubscription instance
     */
    public GHSubscription() {
    }

    private String createdAt, url, repositoryUrl, reason;
    private boolean subscribed, ignored;

    private GHRepository repo;

    /**
     * Gets created at.
     *
     * @return the created at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getCreatedAt() {
        return GitHubClient.parseInstant(createdAt);
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
        return repositoryUrl;
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
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
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
        root().createRequest().method("DELETE").withUrlPath(repo.getApiTailUrl("subscription")).send();
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

package org.kohsuke.github;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The type GHDeployKey.
 */
public class GHDeployKey {

    /** Name of user that added the deploy key */
    private String added_by;

    /** Creation date of the deploy key */
    private String created_at;

    /** Last used date of the deploy key */
    private String last_used;

    private GHRepository owner;
    /** Whether the deploykey has readonly permission or full access */
    private boolean read_only;

    /** The id. */
    protected long id;

    /** The title. */
    protected String url, key, title;

    /** The verified. */
    protected boolean verified;

    /**
     * Create default GHDeployKey instance
     */
    public GHDeployKey() {
    }

    /**
     * Delete.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(String.format("/repos/%s/%s/keys/%d", owner.getOwnerName(), owner.getName(), id))
                .send();
    }

    /**
     * Gets added_by
     *
     * @return the added_by
     */
    public String getAdded_by() {
        return added_by;
    }

    /**
     * Gets created_at.
     *
     * @return the created_at
     */
    public Date getCreatedAt() {
        return GitHubClient.parseDate(created_at);
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets last_used.
     *
     * @return the last_used
     */
    public Date getLastUsedAt() {
        return GitHubClient.parseDate(last_used);
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
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
     * Is read_only
     *
     * @return true if the key can only read. False if the key has write permission as well.
     */
    public boolean isRead_only() {
        return read_only;
    }

    /**
     * Is verified boolean.
     *
     * @return the boolean
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
        return new ToStringBuilder(this).append("title", title)
                .append("id", id)
                .append("key", key)
                .append("created_at", created_at)
                .append("last_used", last_used)
                .append("added_by", added_by)
                .append("read_only", read_only)
                .toString();
    }

    /**
     * Wrap gh deploy key.
     *
     * @param repo
     *            the repo
     * @return the gh deploy key
     */
    GHDeployKey lateBind(GHRepository repo) {
        this.owner = repo;
        return this;
    }
}

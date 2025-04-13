package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The type GHDeployKey.
 */
public class GHDeployKey {

    /** Name of user that added the deploy key */
    private String addedBy;

    /** Creation date of the deploy key */
    private String createdAt;

    /** Last used date of the deploy key */
    private String lastUsed;

    private GHRepository owner;
    /** Whether the deploykey has readonly permission or full access */
    private boolean readOnly;

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
    public String getAddedBy() {
        return addedBy;
    }

    /**
     * Gets added_by
     *
     * @return the added_by
     * @deprecated Use {@link #getAddedBy()}
     */
    @Deprecated
    public String getAdded_by() {
        return getAddedBy();
    }

    /**
     * Gets createdAt.
     *
     * @return the createdAt
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getCreatedAt() {
        return GitHubClient.parseInstant(createdAt);
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
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getLastUsedAt() {
        return GitHubClient.parseInstant(lastUsed);
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
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Is read_only
     *
     * @return true if the key can only read. False if the key has write permission as well.
     * @deprecated {@link #isReadOnly()}
     */
    @Deprecated
    public boolean isRead_only() {
        return isReadOnly();
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
                .append("created_at", createdAt)
                .append("last_used", lastUsed)
                .append("added_by", addedBy)
                .append("read_only", readOnly)
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

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;

/**
 * Represents a GitHub repository autolink reference.
 *
 * @author Alaurant
 * @see GHAutolinkBuilder
 * @see GHRepository#listAutolinks() GHRepository#listAutolinks()
 * @see <a href="https://docs.github.com/en/rest/repos/autolinks">Repository autolinks API</a>
 */
public class GHAutolink {

    private int id;
    private String key_prefix;
    private String url_template;
    private boolean is_alphanumeric;
    private GHRepository owner;

    /**
     * Instantiates a new Gh autolink.
     */
    public GHAutolink() {
    }

    /**
     * Gets the autolink ID
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the key prefix used to identify issues/PR references
     *
     * @return the key prefix string
     */
    public String getKeyPrefix() {
        return key_prefix;
    }

    /**
     * Gets the URL template that will be used for matching
     *
     * @return the URL template string
     */
    public String getUrlTemplate() {
        return url_template;
    }

    /**
     * Checks if the autolink uses alphanumeric values
     *
     * @return true if alphanumeric, false otherwise
     */
    public boolean isAlphanumeric() {
        return is_alphanumeric;
    }

    /**
     * Gets the repository that owns this autolink
     *
     * @return the repository instance
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Deletes this autolink
     *
     * @throws IOException
     *             if the deletion fails
     */
    public void delete() throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(String.format("/repos/%s/%s/autolinks/%d", owner.getOwnerName(), owner.getName(), getId()))
                .send();
    }

    /**
     * Wraps this autolink with its owner repository.
     *
     * @param owner
     *            the repository that owns this autolink
     * @return this instance
     */
    GHAutolink lateBind(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}

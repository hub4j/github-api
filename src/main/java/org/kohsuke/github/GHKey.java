package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * SSH public key.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHKey extends GitHubInteractiveObject {

    /** The title. */
    protected String url, key, title;

    /** The verified. */
    protected boolean verified;

    /** The id. */
    protected int id;

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
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
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Something like "https://api.github.com/user/keys/73593"
     *
     * @return the url
     */
    public String getUrl() {
        return url;
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
        return new ToStringBuilder(this).append("title", title).append("id", id).append("key", key).toString();
    }

    /**
     * Delete the GHKey
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(String.format("/user/keys/%d", id)).send();
    }
}

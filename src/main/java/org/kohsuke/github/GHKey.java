package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * SSH public key.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHKey {
    /* package almost final */ GitHub root;

    protected String url, key, title;
    protected boolean verified;
    protected int id;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Something like "https://api.github.com/user/keys/73593"
     */
    public String getUrl() {
        return url;
    }

    public GitHub getRoot() {
        return root;
    }

    public boolean isVerified() {
        return verified;
    }

    /* package */ GHKey wrap(GitHub root) {
        this.root = root;
        return this;
    }

    public String toString() {
        return new ToStringBuilder(this).append("title", title).append("id", id).append("key", key).toString();
    }
}

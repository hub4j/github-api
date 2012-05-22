package org.kohsuke.github;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * SSH public key.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHKey {
    /*package almost final*/ GitHub root;

    private String url, key, title;
    private boolean verified;
    private int id;

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

    public boolean isVerified() {
        return verified;
    }

    /*package*/ GHKey wrap(GitHub root) {
        this.root = root;
        return this;
    }

    public String toString() {
        return new ToStringBuilder(this).append("title",title).append("id",id).append("key",key).toString();
    }
}

package org.kohsuke.github;

import java.net.URL;

/**
 * Provides information on a Git ref from GitHub.
 *
 * @author Michael Clarke
 */
public class GHRef {

    private String ref, url;
    private GHObject object;

    public String getRef() {
        return ref;
    }

    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    public GHObject getObject() {
        return object;
    }


    public static class GHObject {
         private String type, sha, url;

        public String getType() {
            return type;
        }

        public String getSha() {
            return sha;
        }

        public URL getUrl() {
            return GitHub.parseURL(url);
        }
    }
}

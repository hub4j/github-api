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

    /**
     * Name of the ref, such as "refs/tags/abc"
     */
    public String getRef() {
        return ref;
    }

    /**
     * The API URL of this tag, such as https://api.github.com/repos/jenkinsci/jenkins/git/refs/tags/1.312
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * The object that this ref points to.
     */
    public GHObject getObject() {
        return object;
    }


    public static class GHObject {
         private String type, sha, url;

        /**
         * Type of the object, such as "commit"
         */
        public String getType() {
            return type;
        }

        /**
         * SHA1 of this object.
         */
        public String getSha() {
            return sha;
        }

        /**
         * API URL to this Git data, such as https://api.github.com/repos/jenkinsci/jenkins/git/commits/b72322675eb0114363a9a86e9ad5a170d1d07ac0
         */
        public URL getUrl() {
            return GitHub.parseURL(url);
        }
    }
}

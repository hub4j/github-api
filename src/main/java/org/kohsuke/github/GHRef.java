package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

/**
 * Provides information on a Git ref from GitHub.
 *
 * @author Michael Clarke
 */
public class GHRef {
    /* package almost final */ GitHub root;

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

    /**
     * Updates this ref to the specified commit.
     *
     * @param sha
     *            The SHA1 value to set this reference to
     */
    public void updateTo(String sha) throws IOException {
        updateTo(sha, false);
    }

    /**
     * Updates this ref to the specified commit.
     *
     * @param sha
     *            The SHA1 value to set this reference to
     * @param force
     *            Whether or not to force this ref update.
     */
    public void updateTo(String sha, Boolean force) throws IOException {
        new Requester(root).with("sha", sha).with("force", force).method("PATCH").to(url, GHRef.class).wrap(root);
    }

    /**
     * Deletes this ref from the repository using the GitHub API.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(url);
    }

    GHRef wrap(GitHub root) {
        this.root = root;
        return this;
    }

    static GHRef[] wrap(GHRef[] in, GitHub root) {
        for (GHRef r : in) {
            r.wrap(root);
        }
        return in;
    }

    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
            "NP_UNWRITTEN_FIELD" }, justification = "JSON API")
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
         * API URL to this Git data, such as
         * https://api.github.com/repos/jenkinsci/jenkins/git/commits/b72322675eb0114363a9a86e9ad5a170d1d07ac0
         */
        public URL getUrl() {
            return GitHub.parseURL(url);
        }
    }
}

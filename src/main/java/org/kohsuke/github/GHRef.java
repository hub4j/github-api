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
     *
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * The API URL of this tag, such as https://api.github.com/repos/jenkinsci/jenkins/git/refs/tags/1.312
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * The object that this ref points to.
     *
     * @return the object
     */
    public GHObject getObject() {
        return object;
    }

    /**
     * Updates this ref to the specified commit.
     *
     * @param sha
     *            The SHA1 value to set this reference to
     * @throws IOException
     *             the io exception
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
     * @throws IOException
     *             the io exception
     */
    public void updateTo(String sha, Boolean force) throws IOException {
        root.retrieve()
                .method("PATCH")
                .with("sha", sha)
                .with("force", force)
                .withUrlPath(url)
                .to(GHRef.class)
                .wrap(root);
    }

    /**
     * Deletes this ref from the repository using the GitHub API.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").withUrlPath(url).to();
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

    /**
     * The type GHObject.
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class GHObject {
        private String type, sha, url;

        /**
         * Type of the object, such as "commit"
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * SHA1 of this object.
         *
         * @return the sha
         */
        public String getSha() {
            return sha;
        }

        /**
         * API URL to this Git data, such as
         * https://api.github.com/repos/jenkinsci/jenkins/git/commits/b72322675eb0114363a9a86e9ad5a170d1d07ac0
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHub.parseURL(url);
        }
    }
}

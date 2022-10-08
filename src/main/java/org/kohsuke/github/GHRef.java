package org.kohsuke.github;

import com.fasterxml.jackson.databind.JsonMappingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * Provides information on a Git ref from GitHub.
 *
 * @author Michael Clarke
 */
public class GHRef extends GitHubInteractiveObject {
    private String ref, url;
    private GHObject object;

    /**
     * Name of the ref, such as "refs/tags/abc".
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
        return GitHubClient.parseURL(url);
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
        root().createRequest()
                .method("PATCH")
                .with("sha", sha)
                .with("force", force)
                .withUrlPath(url)
                .fetch(GHRef.class);
    }

    /**
     * Deletes this ref from the repository using the GitHub API.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(url).send();
    }

    /**
     * Retrive a ref of the given type for the current GitHub repository.
     *
     * @param repository
     *            the repository to read from
     * @param refName
     *            eg: heads/branch
     * @return refs matching the request type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    static GHRef read(GHRepository repository, String refName) throws IOException {
        // Also accept e.g. "refs/heads/branch" for consistency with createRef().
        if (refName.startsWith("refs/")) {
            refName = refName.replaceFirst("refs/", "");
        }

        // We would expect this to use `git/ref/%s` but some versions of GHE seem to not support it
        // Instead use `git/refs/%s` and check the result actually matches the ref
        GHRef result = null;
        try {
            result = repository.root()
                    .createRequest()
                    .withUrlPath(repository.getApiTailUrl(String.format("git/refs/%s", refName)))
                    .fetch(GHRef.class);
        } catch (IOException e) {
            // If the parse exception is due to the above returning an array instead of a single ref
            // that means the individual ref did not exist. Handled by result check below.
            // Otherwise, rethrow.
            if (!(e.getCause() instanceof JsonMappingException)) {
                throw e;
            }
        }

        // Verify that the ref returned is the one requested
        // Used .endsWith(refName) instead of .equals("refs/" + refName) to workaround a GitBucket
        // issue where the "ref" field omits the "refs/" prefix. "endsWith()" is functionally
        // the same for this scenario - the server refs matching is prefix-based, so
        // a ref that ends with the correct string will always be the correct one.
        if (result == null || !result.getRef().endsWith(refName)) {
            throw new GHFileNotFoundException(String.format("git/refs/%s", refName)
                    + " {\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/git/refs/#get-a-reference\"}");
        }
        return result;
    }

    /**
     * Retrieves all refs of the given type for the current GitHub repository.
     *
     * @param repository
     *            the repository to read from
     * @param refType
     *            the type of reg to search for e.g. <code>tags</code> or <code>commits</code>
     * @return paged iterable of all refs of the specified type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    static PagedIterable<GHRef> readMatching(GHRepository repository, String refType) throws IOException {
        if (refType.startsWith("refs/")) {
            refType = refType.replaceFirst("refs/", "");
        }

        String url = repository.getApiTailUrl(String.format("git/refs/%s", refType));
        // if no types, do not end with slash just to be safe.
        if (refType.equals("")) {
            url = url.substring(0, url.length() - 1);
        }
        return repository.root().createRequest().withUrlPath(url).toIterable(GHRef[].class, item -> repository.root());
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
         * Type of the object, such as "commit".
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
            return GitHubClient.parseURL(url);
        }
    }
}

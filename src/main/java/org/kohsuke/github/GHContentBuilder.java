package org.kohsuke.github;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Used to create/update content.
 *
 * <p>
 * Call various methods to build up parameters, then call {@link #commit()} to make the change effective.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#createContent() GHRepository#createContent()
 */
public final class GHContentBuilder {
    private final GHRepository repo;
    private final Requester req;
    private String path;

    GHContentBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = new Requester(repo.root).method("PUT");
    }

    /**
     * Path gh content builder.
     *
     * @param path
     *            the path
     * @return the gh content builder
     */
    public GHContentBuilder path(String path) {
        this.path = path;
        req.with("path", path);
        return this;
    }

    /**
     * Branch gh content builder.
     *
     * @param branch
     *            the branch
     * @return the gh content builder
     */
    public GHContentBuilder branch(String branch) {
        req.with("branch", branch);
        return this;
    }

    /**
     * Used when updating (but not creating a new content) to specify Thetblob SHA of the file being replaced.
     *
     * @param sha
     *            the sha
     * @return the gh content builder
     */
    public GHContentBuilder sha(String sha) {
        req.with("sha", sha);
        return this;
    }

    /**
     * Content gh content builder.
     *
     * @param content
     *            the content
     * @return the gh content builder
     */
    public GHContentBuilder content(byte[] content) {
        req.with("content", Base64.encodeBase64String(content));
        return this;
    }

    /**
     * Content gh content builder.
     *
     * @param content
     *            the content
     * @return the gh content builder
     */
    public GHContentBuilder content(String content) {
        try {
            return content(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException x) {
            throw new AssertionError();
        }
    }

    /**
     * Message gh content builder.
     *
     * @param commitMessage
     *            the commit message
     * @return the gh content builder
     */
    public GHContentBuilder message(String commitMessage) {
        req.with("message", commitMessage);
        return this;
    }

    /**
     * Commits a new content.
     *
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse commit() throws IOException {
        GHContentUpdateResponse response = req.to(GHContent.getApiRoute(repo, path), GHContentUpdateResponse.class);

        response.getContent().wrap(repo);
        response.getCommit().wrapUp(repo);

        return response;
    }
}

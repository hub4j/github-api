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
 * @see GHRepository#createContent()
 */
public final class GHContentBuilder {
    private final GHRepository repo;
    private final Requester req;
    private String path;

    GHContentBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = repo.root.createRequester().method("PUT");
    }

    public GHContentBuilder path(String path) {
        this.path = path;
        req.with("path",path);
        return this;
    }

    public GHContentBuilder branch(String branch) {
        req.with("branch", branch);
        return this;
    }

    /**
     * Used when updating (but not creating a new content) to specify
     * Thetblob SHA of the file being replaced.
     */
    public GHContentBuilder sha(String sha) {
        req.with("sha", sha);
        return this;
    }

    public GHContentBuilder content(byte[] content) {
        req.with("content", Base64.encodeBase64String(content));
        return this;
    }

    public GHContentBuilder content(String content) {
        try {
            return content(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException x) {
            throw new AssertionError();
        }
    }

    public GHContentBuilder message(String commitMessage) {
        req.with("message", commitMessage);
        return this;
    }

    /**
     * Commits a new content.
     */
    public GHContentUpdateResponse commit() throws IOException {
        GHContentUpdateResponse response = req.to(repo.getApiTailUrl("contents/" + path), GHContentUpdateResponse.class);

        response.getContent().wrap(repo);
        response.getCommit().wrapUp(repo);

        return response;
    }
}

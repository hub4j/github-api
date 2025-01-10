package org.kohsuke.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// TODO: Auto-generated Javadoc
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

    private static final class UserInfo {
        private final String name;
        private final String email;

        private UserInfo(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    /**
     * Instantiates a new GH content builder.
     *
     * @param repo
     *            the repo
     */
    GHContentBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = repo.root().createRequest().method("PUT");
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
     * Used when updating (but not creating a new content) to specify the blob SHA of the file being replaced.
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
        req.with("content", Base64.getEncoder().encodeToString(content));
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
        return content(content.getBytes(StandardCharsets.UTF_8));
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
     * Configures the author of this content.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @return the gh commit builder
     */
    public GHContentBuilder author(String name, String email) {
        req.with("author", new UserInfo(name, email));
        return this;
    }

    /**
     * Configures the committer of this content.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @return the gh commit builder
     */
    public GHContentBuilder committer(String name, String email) {
        req.with("committer", new UserInfo(name, email));
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
        GHContentUpdateResponse response = req.withUrlPath(GHContent.getApiRoute(repo, path))
                .fetch(GHContentUpdateResponse.class);

        response.getContent().wrap(repo);
        response.getCommit().wrapUp(repo);

        return response;
    }
}

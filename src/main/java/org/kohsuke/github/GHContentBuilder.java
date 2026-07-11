package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

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
    @JsonInclude(Include.NON_NULL)
    private static final class UserInfo {
        private final String date;
        private final String email;
        private final String name;

        private UserInfo(String name, String email, Instant date) {
            this.name = name;
            this.email = email;
            this.date = date != null ? GitHubClient.printInstant(date) : null;
        }
    }

    private String path;
    private final GHRepository repo;
    private final Requester req;

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
     * Configures the author of the commit. If not specified, the authenticated user is used as the author.
     *
     * @param name
     *            the name of the author
     * @param email
     *            the email of the author
     * @return the gh content builder
     */
    public GHContentBuilder author(String name, String email) {
        return author(name, email, (Instant) null);
    }

    /**
     * Configures the author of the commit. If not specified, the authenticated user is used as the author.
     *
     * @param name
     *            the name of the author
     * @param email
     *            the email of the author
     * @param date
     *            the date of the authoring
     * @return the gh content builder
     * @deprecated use {@link #author(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentBuilder author(String name, String email, Date date) {
        return author(name, email, GitHubClient.toInstantOrNull(date));
    }

    /**
     * Configures the author of the commit. If not specified, the authenticated user is used as the author.
     *
     * @param name
     *            the name of the author
     * @param email
     *            the email of the author
     * @param date
     *            the timestamp for the authoring
     * @return the gh content builder
     */
    public GHContentBuilder author(String name, String email, Instant date) {
        req.with("author", new UserInfo(name, email, date));
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

    /**
     * Configures the committer of the commit. If not specified, the authenticated user is used as the committer.
     *
     * @param name
     *            the name of the committer
     * @param email
     *            the email of the committer
     * @return the gh content builder
     */
    public GHContentBuilder committer(String name, String email) {
        return committer(name, email, (Instant) null);
    }

    /**
     * Configures the committer of the commit. If not specified, the authenticated user is used as the committer.
     *
     * @param name
     *            the name of the committer
     * @param email
     *            the email of the committer
     * @param date
     *            the date of the commit
     * @return the gh content builder
     * @deprecated use {@link #committer(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentBuilder committer(String name, String email, Date date) {
        return committer(name, email, GitHubClient.toInstantOrNull(date));
    }

    /**
     * Configures the committer of the commit. If not specified, the authenticated user is used as the committer.
     *
     * @param name
     *            the name of the committer
     * @param email
     *            the email of the committer
     * @param date
     *            the timestamp of the commit
     * @return the gh content builder
     */
    public GHContentBuilder committer(String name, String email, Instant date) {
        req.with("committer", new UserInfo(name, email, date));
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
}

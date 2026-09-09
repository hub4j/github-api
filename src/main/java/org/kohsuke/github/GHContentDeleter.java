package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

/**
 * Builder for deleting repository content with support for specifying author and committer information.
 *
 * <p>
 * Obtain an instance via {@link GHContent#createDelete()}.
 *
 * @see GHContent#createDelete()
 */
public final class GHContentDeleter {
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

    private final GHContent content;
    private final Requester req;

    GHContentDeleter(GHContent content) {
        this.content = content;
        final GHRepository repository = content.getOwner();
        this.req = repository.root()
                .createRequest()
                .method("DELETE")
                .inBody()
                .with("path", content.getPath())
                .with("sha", content.getSha());
    }

    /**
     * Configures the author of the commit. If not specified, the authenticated user is used as the author.
     *
     * @param name
     *            the name of the author
     * @param email
     *            the email of the author
     * @return this deleter
     */
    public GHContentDeleter author(String name, String email) {
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
     * @return this deleter
     * @deprecated use {@link #author(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentDeleter author(String name, String email, Date date) {
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
     * @return this deleter
     */
    public GHContentDeleter author(String name, String email, Instant date) {
        req.with("author", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Sets the branch to delete the content from.
     *
     * @param branch
     *            the branch name
     * @return this deleter
     */
    public GHContentDeleter branch(String branch) {
        req.with("branch", branch);
        return this;
    }

    /**
     * Commits the deletion.
     *
     * @return the response containing the commit information
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse commit() throws IOException {
        final GHRepository repository = content.getOwner();
        GHContentUpdateResponse response = req.withUrlPath(GHContent.getApiRoute(repository, content.getPath()))
                .fetch(GHContentUpdateResponse.class);

        response.getCommit().wrapUp(repository);
        return response;
    }

    /**
     * Configures the committer of the commit. If not specified, the authenticated user is used as the committer.
     *
     * @param name
     *            the name of the committer
     * @param email
     *            the email of the committer
     * @return this deleter
     */
    public GHContentDeleter committer(String name, String email) {
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
     * @return this deleter
     * @deprecated use {@link #committer(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentDeleter committer(String name, String email, Date date) {
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
     * @return this deleter
     */
    public GHContentDeleter committer(String name, String email, Instant date) {
        req.with("committer", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Sets the commit message.
     *
     * @param message
     *            the commit message
     * @return this deleter
     */
    public GHContentDeleter message(String message) {
        req.with("message", message);
        return this;
    }
}

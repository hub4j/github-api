package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Builder for updating existing repository content with support for specifying author and committer information.
 *
 * <p>
 * Obtain an instance via {@link GHContent#createUpdate()}.
 *
 * @see GHContent#createUpdate()
 */
public final class GHContentUpdater {
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
    private String encodedContent;
    private final Requester req;

    GHContentUpdater(GHContent content) {
        this.content = content;
        final GHRepository repository = content.getOwner();
        this.req = repository.root()
                .createRequest()
                .method("PUT")
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
     * @return this updater
     */
    public GHContentUpdater author(String name, String email) {
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
     * @return this updater
     * @deprecated use {@link #author(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentUpdater author(String name, String email, Date date) {
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
     * @return this updater
     */
    public GHContentUpdater author(String name, String email, Instant date) {
        req.with("author", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Sets the branch to update the content on.
     *
     * @param branch
     *            the branch name
     * @return this updater
     */
    public GHContentUpdater branch(String branch) {
        req.with("branch", branch);
        return this;
    }

    /**
     * Commits the update.
     *
     * @return the response containing the updated content and commit information
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse commit() throws IOException {
        final GHRepository repository = content.getOwner();
        GHContentUpdateResponse response = req.withUrlPath(GHContent.getApiRoute(repository, content.getPath()))
                .fetch(GHContentUpdateResponse.class);

        response.getContent().wrap(repository);
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
     * @return this updater
     */
    public GHContentUpdater committer(String name, String email) {
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
     * @return this updater
     * @deprecated use {@link #committer(String, String, Instant)} instead
     */
    @Deprecated
    public GHContentUpdater committer(String name, String email, Date date) {
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
     * @return this updater
     */
    public GHContentUpdater committer(String name, String email, Instant date) {
        req.with("committer", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Sets the new file content as a string.
     *
     * @param newContent
     *            the new content
     * @return this updater
     */
    public GHContentUpdater content(String newContent) {
        return content(newContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sets the new file content as raw bytes.
     *
     * @param newContent
     *            the new content bytes
     * @return this updater
     */
    public GHContentUpdater content(byte[] newContent) {
        this.encodedContent = Base64.getEncoder().encodeToString(newContent);
        req.with("content", encodedContent);
        return this;
    }

    /**
     * Sets the commit message.
     *
     * @param message
     *            the commit message
     * @return this updater
     */
    public GHContentUpdater message(String message) {
        req.with("message", message);
        return this;
    }
}

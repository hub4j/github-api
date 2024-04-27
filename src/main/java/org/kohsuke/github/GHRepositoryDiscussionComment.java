package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * A discussion comment in the repository.
 * <p>
 * This is different from Teams discussions (see {@link GHDiscussion}).
 * <p>
 * The discussion_comment event exposes the GraphQL object (more or less - the ids are handled differently for instance)
 * directly. The new Discussions API is only available through GraphQL so for now you cannot execute any actions on this
 * object.
 *
 * @author Guillaume Smet
 * @see <a href="https://docs.github.com/en/graphql/guides/using-the-graphql-api-for-discussions#discussion">The GraphQL
 *      API for Discussions</a>
 */
public class GHRepositoryDiscussionComment extends GHObject {

    private String htmlUrl;

    private Long parentId;
    private int childCommentCount;

    private GHUser user;
    private GHCommentAuthorAssociation authorAssociation;
    private String body;

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets the parent comment id.
     *
     * @return the parent comment id
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * Gets the number of child comments.
     *
     * @return the number of child comments
     */
    public int getChildCommentCount() {
        return childCommentCount;
    }

    /**
     * Gets the user.
     *
     * @return the user
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public GHUser getUser() throws IOException {
        return root().intern(user);
    }

    /**
     * Gets the author association.
     *
     * @return the author association
     */
    public GHCommentAuthorAssociation getAuthorAssociation() {
        return authorAssociation;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }
}

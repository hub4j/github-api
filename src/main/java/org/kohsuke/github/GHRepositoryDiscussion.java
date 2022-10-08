package org.kohsuke.github;

import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * A discussion in the repository.
 * <p>
 * This is different from Teams discussions (see {@link GHDiscussion}).
 * <p>
 * The discussion event exposes the GraphQL object (more or less - the ids are handled differently for instance)
 * directly. The new Discussions API is only available through GraphQL so for now you cannot execute any actions on this
 * object.
 *
 * @author Guillaume Smet
 * @see <a href="https://docs.github.com/en/graphql/guides/using-the-graphql-api-for-discussions#discussion">The GraphQL
 *      API for Discussions</a>
 */
public class GHRepositoryDiscussion extends GHObject {

    private Category category;

    private String answerHtmlUrl;

    private String answerChosenAt;
    private GHUser answerChosenBy;
    private String htmlUrl;

    private int number;
    private String title;
    private GHUser user;
    private String state;
    private boolean locked;
    private int comments;
    private GHCommentAuthorAssociation authorAssociation;
    private String activeLockReason;
    private String body;
    private String timelineUrl;

    /**
     * Gets the category.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the answer html url.
     *
     * @return the answer html url
     */
    public URL getAnswerHtmlUrl() {
        return GitHubClient.parseURL(answerHtmlUrl);
    }

    /**
     * Gets the answer chosen at.
     *
     * @return the answer chosen at
     */
    public Date getAnswerChosenAt() {
        return GitHubClient.parseDate(answerChosenAt);
    }

    /**
     * Gets the answer chosen by.
     *
     * @return the answer chosen by
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public GHUser getAnswerChosenBy() throws IOException {
        return root().intern(answerChosenBy);
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
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
     * Gets the state.
     *
     * @return the state
     */
    public State getState() {
        return EnumUtils.getEnumOrDefault(State.class, state, State.UNKNOWN);
    }

    /**
     * Checks if is locked.
     *
     * @return true, if is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Gets the comments.
     *
     * @return the comments
     */
    public int getComments() {
        return comments;
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
     * Gets the active lock reason.
     *
     * @return the active lock reason
     */
    public String getActiveLockReason() {
        return activeLockReason;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the timeline url.
     *
     * @return the timeline url
     */
    public String getTimelineUrl() {
        return timelineUrl;
    }

    /**
     * Category of a discussion.
     * <p>
     * Note that while it is relatively close to the GraphQL objects, some of the fields such as the id are handled
     * differently.
     *
     * @see <a href=
     *      "https://docs.github.com/en/graphql/guides/using-the-graphql-api-for-discussions#discussioncategory">The
     *      GraphQL API for Discussions</a>
     */
    public static class Category {

        private long id;
        private String nodeId;
        private long repositoryId;
        private String emoji;
        private String name;
        private String description;
        private String createdAt;
        private String updatedAt;
        private String slug;
        private boolean isAnswerable;

        /**
         * Gets the id.
         *
         * @return the id
         */
        public long getId() {
            return id;
        }

        /**
         * Gets the node id.
         *
         * @return the node id
         */
        public String getNodeId() {
            return nodeId;
        }

        /**
         * Gets the repository id.
         *
         * @return the repository id
         */
        public long getRepositoryId() {
            return repositoryId;
        }

        /**
         * Gets the emoji.
         *
         * @return the emoji
         */
        public String getEmoji() {
            return emoji;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the created at.
         *
         * @return the created at
         */
        public Date getCreatedAt() {
            return GitHubClient.parseDate(createdAt);
        }

        /**
         * Gets the updated at.
         *
         * @return the updated at
         */
        public Date getUpdatedAt() {
            return GitHubClient.parseDate(updatedAt);
        }

        /**
         * Gets the slug.
         *
         * @return the slug
         */
        public String getSlug() {
            return slug;
        }

        /**
         * Checks if is answerable.
         *
         * @return true, if is answerable
         */
        public boolean isAnswerable() {
            return isAnswerable;
        }
    }

    /**
     * The Enum State.
     */
    public enum State {

        /** The open. */
        OPEN,
        /** The locked. */
        LOCKED,
        /** The unknown. */
        UNKNOWN;
    }
}

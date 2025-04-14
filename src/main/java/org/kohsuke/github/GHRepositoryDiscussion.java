package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.kohsuke.github.internal.EnumUtils;

import java.net.URL;
import java.time.Instant;
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
    public static class Category extends GitHubBridgeAdapterObject {

        private String createdAt;

        private String description;
        private String emoji;
        private long id;
        private boolean isAnswerable;
        private String name;
        private String nodeId;
        private long repositoryId;
        private String slug;
        private String updatedAt;
        /**
         * Create default Category instance
         */
        public Category() {
        }

        /**
         * Gets the created at.
         *
         * @return the created at
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getCreatedAt() {
            return GitHubClient.parseInstant(createdAt);
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
         * Gets the emoji.
         *
         * @return the emoji
         */
        public String getEmoji() {
            return emoji;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public long getId() {
            return id;
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
         * Gets the slug.
         *
         * @return the slug
         */
        public String getSlug() {
            return slug;
        }

        /**
         * Gets the updated at.
         *
         * @return the updated at
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getUpdatedAt() {
            return GitHubClient.parseInstant(updatedAt);
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

        /** The locked. */
        LOCKED,
        /** The open. */
        OPEN,
        /** The unknown. */
        UNKNOWN;
    }

    private String activeLockReason;

    private String answerChosenAt;
    private GHUser answerChosenBy;
    private String answerHtmlUrl;

    private GHCommentAuthorAssociation authorAssociation;
    private String body;
    private Category category;
    private int comments;
    private String htmlUrl;
    private boolean locked;
    private int number;
    private String state;
    private String timelineUrl;
    private String title;

    private GHUser user;

    /**
     * Create default GHRepositoryDiscussion instance
     */
    public GHRepositoryDiscussion() {
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
     * Gets the answer chosen at.
     *
     * @return the answer chosen at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getAnswerChosenAt() {
        return GitHubClient.parseInstant(answerChosenAt);
    }

    /**
     * Gets the answer chosen by.
     *
     * @return the answer chosen by
     */
    public GHUser getAnswerChosenBy() {
        return root().intern(answerChosenBy);
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

    /**
     * Gets the category.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
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
     * Gets the state.
     *
     * @return the state
     */
    public State getState() {
        return EnumUtils.getEnumOrDefault(State.class, state, State.UNKNOWN);
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
     */
    public GHUser getUser() {
        return root().intern(user);
    }

    /**
     * Checks if is locked.
     *
     * @return true, if is locked
     */
    public boolean isLocked() {
        return locked;
    }
}

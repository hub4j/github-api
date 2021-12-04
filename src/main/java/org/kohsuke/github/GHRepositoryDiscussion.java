package org.kohsuke.github;

import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

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

    public Category getCategory() {
        return category;
    }

    public URL getAnswerHtmlUrl() {
        return GitHubClient.parseURL(answerHtmlUrl);
    }

    public Date getAnswerChosenAt() {
        return GitHubClient.parseDate(answerChosenAt);
    }

    public GHUser getAnswerChosenBy() throws IOException {
        return root().intern(answerChosenBy);
    }

    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public GHUser getUser() throws IOException {
        return root().intern(user);
    }

    public State getState() {
        return EnumUtils.getEnumOrDefault(State.class, state, State.UNKNOWN);
    }

    public boolean isLocked() {
        return locked;
    }

    public int getComments() {
        return comments;
    }

    public GHCommentAuthorAssociation getAuthorAssociation() {
        return authorAssociation;
    }

    public String getActiveLockReason() {
        return activeLockReason;
    }

    public String getBody() {
        return body;
    }

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

        public long getId() {
            return id;
        }

        public String getNodeId() {
            return nodeId;
        }

        public long getRepositoryId() {
            return repositoryId;
        }

        public String getEmoji() {
            return emoji;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Date getCreatedAt() {
            return GitHubClient.parseDate(createdAt);
        }

        public Date getUpdatedAt() {
            return GitHubClient.parseDate(updatedAt);
        }

        public String getSlug() {
            return slug;
        }

        public boolean isAnswerable() {
            return isAnswerable;
        }
    }

    public enum State {
        OPEN, LOCKED, UNKNOWN;
    }
}

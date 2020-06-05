package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * A discussion in GitHub Team.
 *
 * @author Charles Moulliard
 * @see <a href="https://developer.github.com/v3/teams/discussions">GitHub Team Discussions</a>
 */
public class GHDiscussion extends GHObject {

    @JacksonInject
    private GitHub root;
    private GHTeam team;
    private long number;
    private String body, title, htmlUrl;

    @JsonProperty(value = "private")
    private boolean isPrivate;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(htmlUrl);
    }

    GHDiscussion wrapUp(GHTeam team) {
        this.team = team;
        return this;
    }

    /**
     * Get the team to which this discussion belongs.
     *
     * @return the team for this discussion
     */
    @Nonnull
    public GHTeam getTeam() {
        return team;
    }

    /**
     * Get the title of the discussion.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The description of this discussion.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * The number of this discussion.
     *
     * @return the number
     */
    public long getNumber() {
        return number;
    }

    /**
     * Whether the discussion is private to the team.
     *
     * @return {@code true} if discussion is private.
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Begins the creation of a new instance.
     *
     * Consumer must call {@link GHDiscussion.Creator#done()} to commit changes.
     *
     * @param team
     *            the team in which the discussion will be created.
     * @return a {@link GHLabel.Creator}
     * @throws IOException
     *             the io exception
     */
    static GHDiscussion.Creator create(GHTeam team) throws IOException {
        return new GHDiscussion.Creator(team);
    }

    static GHDiscussion read(GHTeam team, long discussionNumber) throws IOException {
        return team.root.createRequest()
                .setRawUrlPath(team.getUrl().toString() + "/discussions/" + discussionNumber)
                .fetch(GHDiscussion.class)
                .wrapUp(team);
    }

    static PagedIterable<GHDiscussion> readAll(GHTeam team) throws IOException {
        return team.root.createRequest()
                .setRawUrlPath(team.getUrl().toString() + "/discussions")
                .toIterable(GHDiscussion[].class, item -> item.wrapUp(team));
    }

    /**
     * Begins a batch update
     *
     * Consumer must call {@link GHDiscussion.Updater#done()} to commit changes.
     *
     * @return a {@link GHDiscussion.Updater}
     */
    @Preview
    @Deprecated
    public GHDiscussion.Updater update() {
        return new GHDiscussion.Updater(this);
    }

    /**
     * Begins a single property update.
     *
     * @return a {@link GHDiscussion.Setter}
     */
    @Preview
    @Deprecated
    public GHDiscussion.Setter set() {
        return new GHDiscussion.Setter(this);
    }

    /**
     * Delete the discussion
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        team.root.createRequest()
                .method("DELETE")
                .setRawUrlPath(team.getUrl().toString() + "/discussions/" + number)
                .send();
    }

    /**
     * A {@link GHLabelBuilder} that updates a single property per request
     *
     * {@link #done()} is called automatically after the property is set.
     */
    public static class Setter extends GHDiscussionBuilder<GHDiscussion> {
        private Setter(@Nonnull GHDiscussion base) {
            super(GHDiscussion.class, base.team, base);
            requester.method("PATCH").setRawUrlPath(base.getUrl().toString());
        }
    }

    /**
     * A {@link GHLabelBuilder} that allows multiple properties to be updated per request.
     *
     * Consumer must call {@link #done()} to commit changes.
     */
    public static class Updater extends GHDiscussionBuilder<Updater> {
        private Updater(@Nonnull GHDiscussion base) {
            super(GHDiscussion.Updater.class, base.team, base);
            requester.method("PATCH").setRawUrlPath(base.getUrl().toString());
        }
    }

    /**
     * A {@link GHLabelBuilder} that creates a new {@link GHLabel}
     *
     * Consumer must call {@link #done()} to create the new instance.
     */
    public static class Creator extends GHDiscussionBuilder<Creator> {

        private Creator(@Nonnull GHTeam team) {
            super(GHDiscussion.Creator.class, team, null);
            requester.method("POST").withUrlPath(team.getUrl().toString() + "/discussions");
        }

        @Nonnull
        public Creator private_(boolean value) throws IOException {
            return with("private", value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GHDiscussion that = (GHDiscussion) o;
        return number == that.number && Objects.equals(getUrl(), that.getUrl()) && Objects.equals(team, that.team)
                && Objects.equals(body, that.body) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, number, body, title);
    }
}

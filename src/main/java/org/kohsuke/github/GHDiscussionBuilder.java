package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for creating or updating a discussion.
 *
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@link S}
 *            the same as {@link GHLabel}, this builder will commit changes after each call to
 *            {@link #with(String, Object)}.
 */
class GHDiscussionBuilder<S> extends AbstractBuilder<GHDiscussion, S> {

    private final GHTeam team;

    /**
     *
     * @param intermediateReturnType
     *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If
     *            {@link S} the same as {@link GHDiscussion}, this builder will commit changes after each call to
     *            {@link #with(String, Object)}.
     * @param team
     *            the GitHub team. Updates will be sent to the root of this team.
     * @param baseInstance
     *            instance on which to base this builder. If {@code null} a new instance will be created.
     */
    protected GHDiscussionBuilder(@Nonnull Class<S> intermediateReturnType,
            @Nonnull GHTeam team,
            @CheckForNull GHDiscussion baseInstance) {
        super(GHDiscussion.class, intermediateReturnType, team.root(), baseInstance);

        this.team = team;

        if (baseInstance != null) {
            requester.with("title", baseInstance.getTitle());
            requester.with("body", baseInstance.getBody());
        }
    }

    /**
     * Title for this discussion.
     *
     * @param value
     *            title of discussion
     * @return either a continuing builder or an updated {@link GHDiscussion}
     * @throws IOException
     *             if there is an I/O Exception
     */
    @Nonnull
    public S title(String value) throws IOException {
        return with("title", value);
    }

    /**
     * Body content for this discussion.
     *
     * @param value
     *            body of discussion*
     * @return either a continuing builder or an updated {@link GHDiscussion}
     * @throws IOException
     *             if there is an I/O Exception
     */
    @Nonnull
    public S body(String value) throws IOException {
        return with("body", value);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public GHDiscussion done() throws IOException {
        return super.done().wrapUp(team);
    }
}

package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 *
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@link S}
 *            the same as {@link GHLabel}, this builder will commit changes after each call to
 *            {@link #with(String, Object)}.
 */
class GHLabelBuilder<S> extends AbstractBuilder<GHLabel, S> {

    @Nonnull
    final GHRepository repository;

    /**
     *
     * @param intermediateReturnType
     *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If
     *            {@link S} the same as {@link GHLabel}, this builder will commit changes after each call to
     *            {@link #with(String, Object)}.
     * @param repository
     *            the repository for which the changes will be built.
     */
    GHLabelBuilder(@Nonnull Class<S> intermediateReturnType, @Nonnull GHRepository repository) {
        this(intermediateReturnType, repository, new GHLabel());
    }

    /**
     *
     * @param intermediateReturnType
     *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If
     *            {@link S} the same as {@link GHLabel}, this builder will commit changes after each call to
     *            {@link #with(String, Object)}.
     * @param repository
     *            the repository for which the changes will be built.
     * @param baseInstance
     *            instance on which to base this builder.
     */
    GHLabelBuilder(@Nonnull Class<S> intermediateReturnType,
            @Nonnull GHRepository repository,
            @Nonnull GHLabel baseInstance) {
        super(repository.root, intermediateReturnType, GHLabel.class, baseInstance);
        this.repository = repository;

        requester.with("name", baseInstance.getName());
        requester.with("color", baseInstance.getColor());
        requester.with("description", baseInstance.getDescription());
    }

    @Nonnull
    public S name(String value) throws IOException {
        return with("name", value);
    }

    @Nonnull
    public S color(String value) throws IOException {
        return with("color", value);
    }

    @Nonnull
    public S description(String value) throws IOException {
        return with("description", value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public GHLabel done() throws IOException {
        return super.done().lateBind(repository);
    }
}

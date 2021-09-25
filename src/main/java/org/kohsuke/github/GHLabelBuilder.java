package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@link S}
 *            the same as {@link GHLabel}, this builder will commit changes after each call to
 *            {@link #with(String, Object)}.
 */
class GHLabelBuilder<S> extends AbstractBuilder<GHLabel, S> {

    /**
     *
     * @param intermediateReturnType
     *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If
     *            {@link S} the same as {@link GHLabel}, this builder will commit changes after each call to
     *            {@link #with(String, Object)}.
     * @param root
     *            the GitHub instance to which updates will be sent
     * @param baseInstance
     *            instance on which to base this builder. If {@code null} a new instance will be created.
     */
    protected GHLabelBuilder(@Nonnull Class<S> intermediateReturnType,
            @Nonnull GitHub root,
            @CheckForNull GHLabel baseInstance) {
        super(GHLabel.class, intermediateReturnType, root, baseInstance);

        if (baseInstance != null) {
            requester.with("name", baseInstance.getName());
            requester.with("color", baseInstance.getColor());
            requester.with("description", baseInstance.getDescription());
        }
    }

    @Nonnull
    @BetaApi
    public S name(String value) throws IOException {
        return with("name", value);
    }

    @Nonnull
    @BetaApi
    public S color(String value) throws IOException {
        return with("color", value);
    }

    @Nonnull
    @BetaApi
    public S description(String value) throws IOException {
        return with("description", value);
    }
}

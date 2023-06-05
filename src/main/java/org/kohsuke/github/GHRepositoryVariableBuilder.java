package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The type Gh repository variable builder.
 *
 * @param <S> the type parameter
 */
public class GHRepositoryVariableBuilder<S> extends AbstractBuilder<GHRepositoryVariable, S> {
    /**
     * Instantiates a new GH Repository Variable builder.
     *
     * @param intermediateReturnType Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If            {@link S} the same as {@link GHRepositoryVariable}, this builder will commit changes after each call            to {@link #with(String, Object)}.
     * @param root                   the GitHub instance to which updates will be sent
     * @param baseInstance           instance on which to base this builder. If {@code null} a new instance will be created.
     */
    protected GHRepositoryVariableBuilder(@Nonnull Class<S> intermediateReturnType,
            @Nonnull GitHub root,
            @CheckForNull GHRepositoryVariable baseInstance) {
        super(GHRepositoryVariable.class, intermediateReturnType, root, baseInstance);
        if (baseInstance != null) {
            requester.with("name", baseInstance.getName());
            requester.with("value", baseInstance.getValue());
        }
    }

    /**
     * Name.
     *
     * @param value the value
     * @return the s
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Nonnull
    @BetaApi
    public S name(String value) throws IOException {
        return with("name", value);
    }

    /**
     * Name.
     *
     * @param value the value
     * @return the s
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Nonnull
    @BetaApi
    public S value(String value) throws IOException {
        return with("value", value);
    }
}

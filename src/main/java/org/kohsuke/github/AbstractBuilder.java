package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @param <R>
 *            Final return type for built by this builder returned when {@link #done()}} is called.
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@link S}
 *            the same as {@link R}, this builder will commit changes after each call to {@link #with(String, Object)}.
 */
abstract class AbstractBuilder<R, S> {

    // TODO: Not sure how update-in-place behavior should be controlled, but
    // it certainly can be controlled dynamically down to the instance level or inherited for all children of some
    // connection.
    protected boolean updateInPlace;
    private final Class<R> returnType;
    private final Class<S> intermediateReturnType;
    protected final Requester requester;

    @CheckForNull
    private final R baseInstance;

    /**
     * Creates a builder.
     *
     * @param root
     *            the GitHub instance to connect to.
     * @param intermediateReturnType
     *            the intermediate return type returned by calls to {@link #with(String, Object)}.
     * @param builtReturnType
     *            the final return type for built by this builder returned when {@link #done()}} is called.
     * @param baseInstance
     *            optional instance on which to base this builder.
     */
    protected AbstractBuilder(@Nonnull GitHub root,
            @Nonnull Class<S> intermediateReturnType,
            @Nonnull Class<R> builtReturnType,
            @CheckForNull R baseInstance) {
        this.requester = root.createRequest();
        this.returnType = builtReturnType;
        this.intermediateReturnType = intermediateReturnType;
        this.baseInstance = baseInstance;
        this.updateInPlace = false;
    }

    /**
     * Finishes an update, committing changes.
     *
     * This method may update-in-place or not. Either way it returns the resulting instance.
     *
     * @return an instance with updated current data
     * @throws IOException
     *             if there is an I/O Exception
     */
    @Nonnull
    public R done() throws IOException {
        R result;
        if (updateInPlace && baseInstance != null) {
            result = requester.fetchInto(baseInstance);
        } else {
            result = requester.fetch(returnType);
        }
        return result;
    }

    /**
     * Applies a value to a name for this builder.
     *
     * The internals of this method look terrifying, but they they're actually basically safe due to previous comparison
     * of U and T determined by comparing class instances passed in during construction.
     *
     * If U is the same as T, this cause the builder to commit changes after the first value change and return a T from
     * done().
     *
     * If U is not the same as T, the builder will batch together multiple changes and let the user call done() when
     * they are ready.
     *
     * This little bit of roughness in this base class means all inheriting builders get to create Updater and Setter
     * classes from almost identical code. Creator can be implemented with significant code reuse as well.
     *
     * There is probably a cleaner way to implement this, but I'm not sure what it is right now.
     *
     * @param name
     *            the name of the field
     * @param value
     *            the value of the field
     * @return either a continuing builder or an updated data record
     * @throws IOException
     *             if an I/O error occurs
     */
    @Nonnull
    protected S with(@Nonnull String name, Object value) throws IOException {
        requester.with(name, value);
        if (returnType.equals(intermediateReturnType)) {
            return intermediateReturnType.cast(done());
        } else {
            return intermediateReturnType.cast(this);
        }
    }
}

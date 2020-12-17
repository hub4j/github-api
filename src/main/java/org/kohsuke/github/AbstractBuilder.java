package org.kohsuke.github;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An abstract data object builder/updater.
 *
 * This class can be use to make a Builder that supports both batch and single property changes.
 * <p>
 * Batching looks like this:
 * </p>
 * 
 * <pre>
 * update().someName(value).otherName(value).done()
 * </pre>
 * <p>
 * Single changes look like this:
 * </p>
 * 
 * <pre>
 * set().someName(value);
 * set().otherName(value);
 * </pre>
 * <p>
 * If {@link S} is the same as {@link R}, {@link #with(String, Object)} will commit changes after the first value change
 * and return a {@link R} from {@link #done()}.
 * </p>
 * <p>
 * If {@link S} is not the same as {@link R}, {@link #with(String, Object)} will batch together multiple changes and let
 * the user call {@link #done()} when they are ready.
 *
 * @param <R>
 *            Final return type built by this builder returned when {@link #done()}} is called.
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@link S}
 *            the same as {@link R}, this builder will commit changes after each call to {@link #with(String, Object)}.
 */
abstract class AbstractBuilder<R, S> {

    @Nonnull
    private final Class<R> returnType;

    private final boolean commitChangesImmediately;

    @CheckForNull
    private final R baseInstance;

    @Nonnull
    protected final Requester requester;

    // TODO: Not sure how update-in-place behavior should be controlled
    // However, it certainly can be controlled dynamically down to the instance level or inherited for all children of
    // some
    // connection.
    protected boolean updateInPlace;

    /**
     * Creates a builder.
     *
     * @param root
     *            the GitHub instance to connect to.
     * @param intermediateReturnType
     *            the intermediate return type of type {@link S} returned by calls to {@link #with(String, Object)}.
     *            Must either be equal to {@code builtReturnType} or this instance must be castable to this class. If
     *            not, the constructor will throw {@link IllegalArgumentException}.
     * @param finalReturnType
     *            the final return type for built by this builder returned when {@link #done()}} is called.
     * @param baseInstance
     *            optional instance on which to base this builder.
     */
    protected AbstractBuilder(@Nonnull Class<R> finalReturnType,
            @Nonnull Class<S> intermediateReturnType,
            @Nonnull GitHub root,
            @CheckForNull R baseInstance) {
        this.requester = root.createRequest();
        this.returnType = finalReturnType;
        this.commitChangesImmediately = returnType.equals(intermediateReturnType);
        if (!commitChangesImmediately && !intermediateReturnType.isInstance(this)) {
            throw new IllegalArgumentException(
                    "Argument \"intermediateReturnType\": This instance must be castable to intermediateReturnType or finalReturnType must be equal to intermediateReturnType.");
        }

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
    @BetaApi
    @Deprecated
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
     * If {@link S} is the same as {@link R}, this method will commit changes after the first value change and return a
     * {@link R} from {@link #done()}.
     *
     * If {@link S} is not the same as {@link R}, this method will return an {@link S} and letting the caller batch
     * together multiple changes and call {@link #done()} when they are ready.
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
    @BetaApi
    @Deprecated
    protected S with(@Nonnull String name, Object value) throws IOException {
        requester.with(name, value);
        return continueOrDone();
    }

    /**
     * Chooses whether to return a continuing builder or an updated data record
     *
     * If {@link S} is the same as {@link R}, this method will commit changes after the first value change and return a
     * {@link R} from {@link #done()}.
     *
     * If {@link S} is not the same as {@link R}, this method will return an {@link S} and letting the caller batch
     * together multiple changes and call {@link #done()} when they are ready.
     *
     * @return either a continuing builder or an updated data record
     * @throws IOException
     *             if an I/O error occurs
     */
    @Nonnull
    @BetaApi
    @Deprecated
    protected S continueOrDone() throws IOException {
        // This little bit of roughness in this base class means all inheriting builders get to create Updater and
        // Setter classes from almost identical code. Creator can often be implemented with significant code reuse as
        // well.
        if (commitChangesImmediately) {
            // These casts look strange and risky, but they they're actually guaranteed safe due to the return path
            // being based on the previous comparison of class instances passed to the constructor.
            return (S) done();
        } else {
            return (S) this;
        }
    }
}

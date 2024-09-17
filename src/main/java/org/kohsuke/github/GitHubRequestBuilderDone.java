package org.kohsuke.github;

import java.io.IOException;

/**
 * The done method for data object builder/updater.
 *
 * This interface can be used to make a Builder that supports both batch and single property changes.
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
 *
 * @author Liam Newman
 * @param <R>
 *            Final return type built by this builder returned when {@link #done()}} is called.
 */
public interface GitHubRequestBuilderDone<R> {

    /**
     * Finishes a create or update request, committing changes.
     *
     * This method may update-in-place or not. Either way it returns the resulting instance.
     *
     * @return an instance with updated current data
     * @throws IOException
     *             if there is an I/O Exception
     */
    @BetaApi
    R done() throws IOException;
}

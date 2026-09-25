package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.graphql.response.GHGraphQLError;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Thrown when a GitHub GraphQL request returns a successful HTTP response whose body contains a non-empty
 * {@code errors} array.
 *
 * <p>
 * This exception preserves the structured error list so callers can branch on the error {@link GHGraphQLError#getType()
 * type}, {@link GHGraphQLError#getPath() path}, and other fields. Partial response data is exposed through
 * {@link #getResponseData()} and the originating query through {@link #getQuery()} to ease debugging.
 * </p>
 *
 * <p>
 * HTTP-level failures (4xx/5xx) do not surface as this exception; they continue to be reported as {@link HttpException}
 * or its subclasses, since the response cannot be parsed as a GraphQL payload.
 * </p>
 *
 * @see <a href="https://spec.graphql.org/October2021/#sec-Errors">GraphQL spec — Errors</a>
 */
public class GHGraphQLException extends GHIOException {

    private static final long serialVersionUID = 1L;

    /**
     * Structured GraphQL errors. Marked transient because {@link GHGraphQLError} carries Jackson-populated
     * {@code Object} fields that may not be {@link java.io.Serializable}.
     */
    private final transient List<GHGraphQLError> errors;

    /** Original GraphQL query, when available; useful for debugging. */
    private final String query;

    /** Partial response data, transient because the payload type is unconstrained. */
    private final transient Object responseData;

    /**
     * Instantiates a new GraphQL exception.
     *
     * @param message
     *            human-readable summary suitable for log output
     * @param errors
     *            the structured GraphQL errors returned by the server, never {@code null}
     * @param responseData
     *            the partial {@code data} payload, or {@code null} if the server returned none
     * @param query
     *            the GraphQL query string sent in the originating request, or {@code null} if unknown
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "errors list is wrapped unmodifiable")
    public GHGraphQLException(@Nonnull String message,
            @Nonnull List<GHGraphQLError> errors,
            @CheckForNull Object responseData,
            @CheckForNull String query) {
        super(message);
        this.errors = Collections.unmodifiableList(errors);
        this.responseData = responseData;
        this.query = query;
    }

    /**
     * Get the structured error entries returned by the GraphQL endpoint. The list is unmodifiable. Returns an empty
     * list after this exception has been deserialized across a JVM boundary, since the structured errors are not
     * preserved through serialization.
     *
     * @return the GraphQL errors
     */
    @Nonnull
    public List<GHGraphQLError> getErrors() {
        return errors == null ? Collections.emptyList() : errors;
    }

    /**
     * Get the GraphQL query that produced this response, when available.
     *
     * @return the query string, or {@code null}
     */
    @CheckForNull
    public String getQuery() {
        return query;
    }

    /**
     * Get the partial response data returned alongside the errors, if any. GraphQL allows servers to return both
     * {@code data} and {@code errors} when a request only partially fails. Returns {@code null} when the server
     * returned no data or after deserialization, e.g. across the network.
     *
     * @return the partial response data, or {@code null}
     */
    @CheckForNull
    public Object getResponseData() {
        return responseData;
    }
}

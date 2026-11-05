package org.kohsuke.github.internal.graphql.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A response of GraphQL.
 * <p>
 * This class is used to parse the response of GraphQL.
 * </p>
 *
 * @param <T>
 *            the type of data
 */
public class GHGraphQLResponse<T> {

    /**
     * A GraphQL response with basic Object data type.
     */
    public static class ObjectResponse extends GHGraphQLResponse<Object> {
        /**
         * ObjectResponse constructor.
         *
         * @param data
         *            GraphQL success response
         * @param errors
         *            GraphQL failure response, This will be empty if not fail
         */
        @JsonCreator
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public ObjectResponse(@JsonProperty("data") Object data, @JsonProperty("errors") List<GHGraphQLError> errors) {
            super(data, errors);
        }
    }

    private final T data;

    @Nonnull
    private final List<GHGraphQLError> errors;

    /**
     * GHGraphQLResponse constructor
     *
     * @param data
     *            GraphQL success response
     * @param errors
     *            GraphQL failure response, This will be empty if not fail
     */
    @JsonCreator
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
    public GHGraphQLResponse(@JsonProperty("data") T data, @JsonProperty("errors") List<GHGraphQLError> errors) {
        this.data = data;
        this.errors = errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    /**
     * Build a human-readable summary that lists every error message returned by the server.
     *
     * @param prefix
     *            short headline prepended before the bullet list
     * @return the multi-line summary
     */
    public String buildErrorSummary(String prefix) {
        return errors.stream()
                .map(GHGraphQLError::getMessage)
                .collect(Collectors.joining("\n - ", prefix + ":\n - ", ""));
    }

    /**
     * Get response data.
     *
     * @return GraphQL success response
     * @throws RuntimeException
     *             if the response carried errors. The exception message lists each error message.
     */
    public T getData() {
        if (!isSuccessful()) {
            throw new RuntimeException(buildErrorSummary("Response not successful, data invalid"));
        }

        return data;
    }

    /**
     * Get response data, including any partial data the server returned alongside errors. Unlike {@link #getData()},
     * this method never throws.
     *
     * @return the data payload, or {@code null} if absent
     */
    @CheckForNull
    public T getDataUnchecked() {
        return data;
    }

    /**
     * Get response error message.
     *
     * @return GraphQL error messages from Github Response. Empty list when no errors occurred.
     */
    public List<String> getErrorMessages() {
        return errors.stream().map(GHGraphQLError::getMessage).collect(Collectors.toList());
    }

    /**
     * Get the structured GraphQL errors returned by the server.
     *
     * @return the errors, never {@code null}; empty when the response succeeded
     */
    @Nonnull
    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "errors list is wrapped with Collections.unmodifiableList in the constructor")
    public List<GHGraphQLError> getErrors() {
        return errors;
    }

    /**
     * Is response successful.
     *
     * @return request is succeeded. True when error list is empty.
     */
    public boolean isSuccessful() {
        return errors.isEmpty();
    }
}

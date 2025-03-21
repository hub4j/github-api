package org.kohsuke.github.internal.graphql.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private final T data;

    private final List<GraphQLError> errors;

    /**
     * @param data
     *            GraphQL success response
     * @param errors
     *            GraphQL failure response, This will be empty if not fail
     */
    @JsonCreator
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
    public GHGraphQLResponse(@JsonProperty("data") T data, @JsonProperty("errors") List<GraphQLError> errors) {
        if (errors == null) {
            errors = Collections.emptyList();
        }
        this.data = data;
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * @return request is succeeded. True when error list is empty.
     */
    public boolean isSuccessful() {
        return errors.isEmpty();
    }

    /**
     * @return GraphQL success response
     */
    public T getData() {
        if (!isSuccessful()) {
            throw new RuntimeException("Response not successful, data invalid");
        }

        return data;
    }

    /**
     * @return GraphQL error messages from Github Response. Empty list when no errors occurred.
     */
    public List<String> getErrorMessages() {
        return errors.stream().map(GraphQLError::getMessage).collect(Collectors.toList());
    }

    /**
     * A error of GraphQL response. Minimum implementation for GraphQL error.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "JSON API")
    private static class GraphQLError {
        private String message;

        public String getMessage() {
            return message;
        }
    }

    /**
     * A GraphQL response with basic Object data type.
     */
    public static class ObjectResponse extends GHGraphQLResponse<Object> {
        /**
         * {@inheritDoc}
         */
        @JsonCreator
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public ObjectResponse(@JsonProperty("data") Object data, @JsonProperty("errors") List<GraphQLError> errors) {
            super(data, errors);
        }
    }
}

package org.kohsuke.github.internal.graphql.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
        this.data = data;
        this.errors = errors;
    }

    /**
     * @return request is succeeded
     */
    public Boolean isSuccessful() {
        return errors == null || errors.isEmpty();
    }

    /**
     * @return GraphQL success response
     */
    public T getData() {
        if (!isSuccessful()) {
            throw new RuntimeException("This response is Errors occurred response");
        }

        return data;
    }

    /**
     * @return GraphQL error messages from Github Response
     */
    public List<String> getErrorMessages() {
        if (isSuccessful()) {
            throw new RuntimeException("No errors occurred");
        }

        return errors.stream().map(GraphQLError::getErrorMessage).collect(Collectors.toList());
    }

    /**
     * A error of GraphQL response. Minimum implementation for GraphQL error.
     */
    private static class GraphQLError {

        private final String errorMessage;

        @JsonCreator
        public GraphQLError(@JsonProperty("message") String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

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

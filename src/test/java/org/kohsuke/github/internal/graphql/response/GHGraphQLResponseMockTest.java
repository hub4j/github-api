package org.kohsuke.github.internal.graphql.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test GHGraphQLResponse's methods
 */
class GHGraphQLResponseMockTest {

    private GHGraphQLResponse<Object> convertJsonToGraphQLResponse(String json) throws JsonProcessingException {
        JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

        ObjectReader objectReader = mapper.reader();
        JavaType javaType = objectReader.getTypeFactory()
                .constructParametricType(GHGraphQLResponse.class, Object.class);

        return objectReader.forType(javaType).readValue(json);
    }

    /**
     * Test the {@link GHGraphQLResponse#buildErrorSummary} helper renders one error per line with the configured
     * prefix.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void buildErrorSummaryListsAllErrors() throws JsonProcessingException {
        String json = "{\"data\": null, \"errors\": [" + "{\"message\": \"first failure\"},"
                + "{\"message\": \"second failure\"}" + "]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(json);

        String summary = response.buildErrorSummary("Request failed");
        assertThat(summary, equalTo("Request failed:\n - first failure\n - second failure"));
    }

    /**
     * Test partial-success: GraphQL allows {@code data} and {@code errors} to coexist.
     * {@link GHGraphQLResponse#getData} still throws because errors are present, but
     * {@link GHGraphQLResponse#getDataUnchecked} exposes the partial payload.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void exposesPartialDataAlongsideErrors() throws JsonProcessingException {
        String json = "{\"data\": {\"repository\": {\"name\": \"probot\"}},"
                + "\"errors\": [{\"message\": \"`invalid cursor` does not appear to be a valid cursor.\"}]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(json);

        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getDataUnchecked(), notNullValue());
        assertThat(response.getErrorMessages(), hasSize(1));

        try {
            response.getData();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("does not appear to be a valid cursor"));
        }
    }

    /**
     * Test get data throws exception when response means error and the message lists every error message returned by
     * the server.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     *
     */
    @Test
    void getDataFailure() throws JsonProcessingException {
        String graphQLErrorResponse = "{\"data\": {\"enablePullRequestAutoMerge\": null},\"errors\": [{\"type\": "
                + "\"UNPROCESSABLE\",\"path\": [\"enablePullRequestAutoMerge\"],\"locations\": [{\"line\": 2,"
                + "\"column\": 5}],\"message\": \"hub4j does not have a verified email, which is required to enable "
                + "auto-merging.\"}]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(graphQLErrorResponse);

        try {
            response.getData();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Response not successful, data invalid"));
            assertThat(e.getMessage(), containsString("does not have a verified email"));
        }
    }

    /**
     * Test getErrorMessages returns an empty list when the response has no errors.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void getErrorMessagesFailure() throws JsonProcessingException {
        String graphQLSuccessResponse = "{\"data\": {\"repository\": {\"pullRequest\": {\"id\": "
                + "\"PR_TEMP_GRAPHQL_ID\"}}}}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(graphQLSuccessResponse);

        List<String> errorMessages = response.getErrorMessages();

        assertThat(errorMessages, is(empty()));
    }

    /**
     * Test that unknown fields on the error object don't break deserialization, ensuring forward compatibility with
     * server-side additions.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void ignoresUnknownErrorFields() throws JsonProcessingException {
        String json = "{\"data\": null, \"errors\": [{" + "\"message\": \"oops\","
                + "\"futureField\": \"something the client does not know about yet\"" + "}]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(json);

        assertThat(response.getErrors(), hasSize(1));
        assertThat(response.getErrors().get(0).getMessage(), equalTo("oops"));
    }

    /**
     * Test that all GraphQL spec fields (type, message, path, locations, extensions) are surfaced when the server
     * returns the full error structure used by GitHub's GraphQL API.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void parsesFullGraphQLErrorFields() throws JsonProcessingException {
        String json = "{\"data\": null, \"errors\": [{" + "\"type\": \"RATE_LIMITED\","
                + "\"message\": \"API rate limit exceeded.\","
                + "\"path\": [\"repository\", \"pullRequests\", 3, \"author\"],"
                + "\"locations\": [{\"line\": 7, \"column\": 11}],"
                + "\"extensions\": {\"code\": \"RATE_LIMITED\", \"resetAt\": \"2026-01-01T00:00:00Z\"}" + "}]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(json);

        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors(), hasSize(1));

        GHGraphQLError error = response.getErrors().get(0);
        assertThat(error.getType(), equalTo("RATE_LIMITED"));
        assertThat(error.getMessage(), equalTo("API rate limit exceeded."));
        assertThat(error.getPath(), contains((Object) "repository", "pullRequests", 3, "author"));
        assertThat(error.getLocations(), hasSize(1));
        assertThat(error.getLocations().get(0).getLine(), equalTo(7));
        assertThat(error.getLocations().get(0).getColumn(), equalTo(11));

        Map<String, Object> extensions = error.getExtensions();
        assertThat(extensions, notNullValue());
        assertThat(extensions, hasEntry("code", "RATE_LIMITED"));
        assertThat(extensions, hasEntry("resetAt", "2026-01-01T00:00:00Z"));
    }

    /**
     * Test the spec-minimum case where only {@code message} is present. All other fields must be null and not throw on
     * access.
     *
     * @throws JsonProcessingException
     *             Json parse exception
     */
    @Test
    void parsesMinimalGraphQLError() throws JsonProcessingException {
        String json = "{\"data\": null, \"errors\": [{\"message\": \"oops\"}]}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(json);

        GHGraphQLError error = response.getErrors().get(0);
        assertThat(error.getMessage(), equalTo("oops"));
        assertThat(error.getType(), is(nullValue()));
        assertThat(error.getPath(), is(nullValue()));
        assertThat(error.getLocations(), is(nullValue()));
        assertThat(error.getExtensions(), is(nullValue()));
    }
}

package org.kohsuke.github.graphql.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Test GHGraphQLResponse's methods
 */
class GHGraphQLResponseMockTest {

    /**
     * Test get data throws exception when response means error
     *
     * @throws JsonProcessingException
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
            assertThat(e.getMessage(), containsString("This response is Errors occurred response"));
        }
    }

    /**
     * Test getErrorMessages throws exception when response means not error
     *
     * @throws JsonProcessingException
     */
    @Test
    void getErrorMessagesFailure() throws JsonProcessingException {
        String graphQLSuccessResponse = "{\"data\": {\"repository\": {\"pullRequest\": {\"id\": "
                + "\"PR_TEMP_GRAPHQL_ID\"}}}}";

        GHGraphQLResponse<Object> response = convertJsonToGraphQLResponse(graphQLSuccessResponse);

        try {
            response.getErrorMessages();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("No errors occurred"));
        }
    }

    private GHGraphQLResponse<Object> convertJsonToGraphQLResponse(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ObjectReader objectReader = objectMapper.reader();
        JavaType javaType = objectReader.getTypeFactory()
                .constructParametricType(GHGraphQLResponse.class, Object.class);

        return objectReader.forType(javaType).readValue(json);
    }

}

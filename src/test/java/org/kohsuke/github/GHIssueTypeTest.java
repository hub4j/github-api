package org.kohsuke.github;

import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link GHIssueType}.
 */
public class GHIssueTypeTest {

    /**
     * Verifies issue type JSON mapping on both the issue and the issue type model.
     *
     * @throws Exception
     *             if JSON mapping fails
     */
    @Test
    public void mapsIssueTypeFromIssueResponse() throws Exception {
        String json = "{" + "\"id\":1," + "\"number\":42," + "\"title\":\"Typed issue\"," + "\"type\":{"
                + "\"id\":1001," + "\"node_id\":\"IT_kwDOBb4AAAAAA-k5\"," + "\"name\":\"Bug\","
                + "\"description\":\"An unexpected problem or behavior\"," + "\"color\":\"red\","
                + "\"created_at\":\"2025-03-18T12:00:00Z\"," + "\"updated_at\":\"2025-03-19T12:00:00Z\","
                + "\"is_enabled\":true}" + "}";

        GHIssue issue = GitHub.getMappingObjectReader().forType(GHIssue.class).readValue(json);
        GHIssueType type = issue.getType();

        assertThat(type.getId(), equalTo(1001L));
        assertThat(type.getNodeId(), equalTo("IT_kwDOBb4AAAAAA-k5"));
        assertThat(type.getName(), equalTo("Bug"));
        assertThat(type.getDescription(), equalTo("An unexpected problem or behavior"));
        assertThat(type.getColor(), equalTo("red"));
        assertThat(type.isEnabled(), is(true));
        assertThat(type.getCreatedAt(), equalTo(Instant.parse("2025-03-18T12:00:00Z")));
        assertThat(type.getUpdatedAt(), equalTo(Instant.parse("2025-03-19T12:00:00Z")));
    }

    /**
     * Verifies an issue without an assigned type remains backwards compatible.
     *
     * @throws Exception
     *             if JSON mapping fails
     */
    @Test
    public void mapsIssueWithoutType() throws Exception {
        GHIssue issue = GitHub.getMappingObjectReader()
                .forType(GHIssue.class)
                .readValue("{\"id\":1,\"number\":42,\"title\":\"Untyped issue\",\"type\":null}");

        assertThat(issue.getType(), nullValue());
    }
}

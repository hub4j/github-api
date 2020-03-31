package org.kohsuke.github;

import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.Matchers.*;

public class GHObjectTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(org.toString(),
                containsString(
                        "login=github-api-test-org,location=<null>,blog=<null>,email=<null>,name=<null>,company=<null>,type=Organization,followers=0,following=0"));

        // getResponseHeaderFields is deprecated but we should not break it.
        assertThat(org.getResponseHeaderFields(), notNullValue());
        assertThat(org.getResponseHeaderFields().get("Cache-Control").get(0), is("private, max-age=60, s-maxage=60"));

        // Header field names must be case-insensitive
        assertThat(org.getResponseHeaderFields().containsKey("CacHe-ContrOl"), is(true));

        // The KeySet from header fields should also be case-insensitive
        assertThat(org.getResponseHeaderFields().keySet().contains("CacHe-ControL"), is(true));
        assertThat(org.getResponseHeaderFields().keySet().contains("CacHe-ControL"), is(true));

        assertThat(org.getResponseHeaderFields().get("cachE-cOntrol").get(0), is("private, max-age=60, s-maxage=60"));

        // GitHub has started changing their headers to all lowercase.
        // For this test we want the field names to be with mixed-case (harder to do comparison).
        // Ensure that it remains that way, if test resources are ever refreshed.
        boolean found = false;
        for (String key : org.getResponseHeaderFields().keySet()) {
            if (Objects.equals("Cache-Control", key)) {
                found = true;
                break;
            }
        }
        assertThat("Must have the literal expected string 'Cache-Control' for header field name", found);
    }
}

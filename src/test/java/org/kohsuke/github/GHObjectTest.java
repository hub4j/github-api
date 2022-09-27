package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHObjectTest.
 */
public class GHObjectTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    /**
     * Test to string.
     *
     * @throws Exception the exception
     */
    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(org.toString(),
                containsString(
                        "login=hub4j-test-org,location=<null>,blog=<null>,email=<null>,bio=<null>,name=<null>,company=<null>,type=Organization,followers=0,following=0,hireable=false"));

        // getResponseHeaderFields is deprecated but we should not break it.
        assertThat(org.getResponseHeaderFields(), notNullValue());
        assertThat(org.getResponseHeaderFields().get("Cache-Control").get(0), is("private, max-age=60, s-maxage=60"));
    }
}

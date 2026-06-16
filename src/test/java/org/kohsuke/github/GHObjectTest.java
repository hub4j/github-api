package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHObjectTest.
 */
public class GHObjectTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    /**
     * Create default GHObjectTest instance
     */
    public GHObjectTest() {
    }

    /**
     * Test to string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        String orgString = org.toString();
        assertThat(orgString, containsString("login=hub4j-test-org"));
        assertThat(orgString, containsString("location=<null>"));
        assertThat(orgString, containsString("blog=<null>"));
        assertThat(orgString, containsString("email=<null>"));
        assertThat(orgString, containsString("bio=<null>"));
        assertThat(orgString, containsString("name=<null>"));
        assertThat(orgString, containsString("company=<null>"));
        assertThat(orgString, containsString("type=Organization"));
        assertThat(orgString, containsString("followers=0"));
        assertThat(orgString, containsString("hireable=false"));

        // getResponseHeaderFields is deprecated but we should not break it.
        assertThat(org.getResponseHeaderFields(), notNullValue());
        assertThat(org.getResponseHeaderFields().get("Cache-Control").get(0), is("private, max-age=60, s-maxage=60"));
    }
}

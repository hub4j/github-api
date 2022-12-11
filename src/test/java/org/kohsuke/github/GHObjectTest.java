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
     * @throws Exception
     *             the exception
     */
    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(org.toString(), containsString("login=hub4j-test-org"));
        assertThat(org.toString(), containsString("location=<null>"));
        assertThat(org.toString(), containsString("blog=<null>"));
        assertThat(org.toString(), containsString("email=<null>"));
        assertThat(org.toString(), containsString("bio=<null>"));
        assertThat(org.toString(), containsString("name=<null>"));
        assertThat(org.toString(), containsString("company=<null>"));
        assertThat(org.toString(), containsString("type=Organization"));
        assertThat(org.toString(), containsString("followers=0"));
        assertThat(org.toString(), containsString("hireable=false"));
        
        // getResponseHeaderFields is deprecated but we should not break it.
        assertThat(org.getResponseHeaderFields(), notNullValue());
        assertThat(org.getResponseHeaderFields().get("Cache-Control").get(0), is("private, max-age=60, s-maxage=60"));
    }
}

package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the GitHub App Api Test
 *
 * @author Daniel Baur
 */
public class GHAppExtendedTest extends AbstractGitHubWireMockTest {

    private static final String APP_SLUG = "ghapi-test-app-4";

    /**
     * Gets the GitHub App by its slug.
     *
     * @throws IOException
     *             An IOException has occurred.
     */
    @Test
    public void getAppBySlugTest() throws IOException {
        GHApp app = gitHub.getApp(APP_SLUG);

        assertThat(app.getId(), is((long) 330762));
        assertThat(app.getSlug(), equalTo(APP_SLUG));
        assertThat(app.getName(), equalTo("GHApi Test app 4"));
        assertThat(app.getExternalUrl(), equalTo("https://github.com/organizations/hub4j-test-org"));
        assertThat(app.getHtmlUrl().toString(), equalTo("https://github.com/apps/ghapi-test-app-4"));
        assertThat(app.getDescription(), equalTo("An app to test the GitHub getApp(slug) method."));
    }



}

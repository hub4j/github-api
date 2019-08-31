package org.kohsuke.github;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.notNullValue;

import static org.hamcrest.core.Is.is;

/**
 * @author Kohsuke Kawaguchi
 */
public class GistTest extends AbstractGitHubApiWireMockTest {
    /**
     * CRUD operation.
     */
    @Test
    public void lifecycleTest() throws Exception {
        GHGist gist = gitHub.createGist()
                .public_(false)
                .description("Test Gist")
                .file("abc.txt","abc")
                .file("def.txt","def")
                .create();

        assertThat(gist.getCreatedAt(), is(notNullValue()));

        assertNotNull(gist.getUpdatedAt());
        assertNotNull(gist.getCommentsUrl());
        assertNotNull(gist.getCommitsUrl());
        assertNotNull(gist.getGitPullUrl());
        assertNotNull(gist.getGitPushUrl());
        assertNotNull(gist.getHtmlUrl());

        gist.delete();
    }

    @Test
    public void starTest() throws Exception {

        githubApi.stubFor(
            put(urlEqualTo("/gists/9903708/star"))
                .inScenario("Star Test")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    noContent())
                .willSetStateTo("Starred"));

        githubApi.stubFor(
            get(urlEqualTo("/gists/9903708/star"))
                .inScenario("Star Test")
                .whenScenarioStateIs("Starred")
                .willReturn(
                    noContent())
                .willSetStateTo("Starred"));

        githubApi.stubFor(
            delete(urlEqualTo("/gists/9903708/star"))
                .inScenario("Star Test")
                .whenScenarioStateIs("Starred")
                .willReturn(
                    noContent())
                .willSetStateTo("UnStarred"));


        GHGist gist = gitHub.getGist("9903708");
        assertEquals("rtyler",gist.getOwner().getLogin());


        gist.star();
        assertTrue(gist.isStarred());

        gist.unstar();
        assertFalse(gist.isStarred());

        GHGist newGist = gist.fork();

        try {
            for (GHGist g : gist.listForks()) {
                if (g.equals(newGist)) {
                    // expected to find it in the clone list
                    return;
                }
            }

            fail("Expected to find a newly cloned gist");
        } finally {
            newGist.delete();
        }
    }

    @Test
    public void gistFile() throws Exception {
        GHGist gist = gitHub.getGist("9903708");

        assertTrue(gist.isPublic());

        assertEquals(1,gist.getFiles().size());
        GHGistFile f = gist.getFile("keybase.md");

        assertEquals("text/markdown", f.getType());
        assertEquals("Markdown", f.getLanguage());
        assertTrue(f.getContent().contains("### Keybase proof"));
        assertNotNull(f.getContent());
    }
}

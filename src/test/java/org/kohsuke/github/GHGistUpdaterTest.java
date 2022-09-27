package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHGistUpdaterTest.
 *
 * @author Martin van Zijl
 */
public class GHGistUpdaterTest extends AbstractGitHubWireMockTest {

    private GHGist gist;

    /**
     * Sets the up.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Before
    public void setUp() throws IOException {
        GHGistBuilder builder = new GHGistBuilder(gitHub);
        gist = builder.description("Test for the API")
                .file("unmodified.txt", "Should be unmodified")
                // .file("delete-me.txt", "To be deleted")
                .file("rename-me.py", "print 'hello'")
                .file("update-me.txt", "To be updated")
                .public_(true)
                .create();
    }

    /**
     * Clean up.
     *
     * @throws Exception the exception
     */
    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        gist.delete();
    }

    /**
     * Test git updater.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGitUpdater() throws Exception {
        GHGistUpdater updater = gist.update();
        GHGist updatedGist = updater.description("Description updated by API")
                .addFile("new-file.txt", "Added by updater")
                // .deleteFile("delete-me.txt")
                .renameFile("rename-me.py", "renamed.py")
                .updateFile("update-me.txt", "Content updated by API")
                .update();

        assertThat(updatedGist.getDescription(), equalTo("Description updated by API"));

        Map<String, GHGistFile> files = updatedGist.getFiles();

        // Check that the unmodified file stays intact.
        assertThat(files.get("unmodified.txt"), is(notNullValue()));
        assertThat(files.get("unmodified.txt").getContent(), equalTo("Should be unmodified"));

        // Check that the files are updated as expected.
        // assertFalse("File was not deleted.", files.containsKey("delete-me.txt"));

        assertThat(files.get("new-file.txt"), is(notNullValue()));
        assertThat(files.get("new-file.txt").getContent(), equalTo("Added by updater"));

        assertThat(files.containsKey("rename-me.py"), is(false));
        assertThat(files.get("renamed.py"), is(notNullValue()));
        assertThat(files.get("renamed.py").getContent(), equalTo("print 'hello'"));

        assertThat(files.get("update-me.txt").getContent(), equalTo("Content updated by API"));
    }
}

package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin van Zijl
 */
public class GHGistUpdaterTest extends AbstractGitHubWireMockTest {

    private GHGist gist;

    @Before
    public void setUp() throws IOException {
        GHGistBuilder builder = new GHGistBuilder(gitHub);
        gist = builder.description("Test for the API")
                .file("unmodified.txt", "Should be unmodified")
                //.file("delete-me.txt", "To be deleted")
                .file("rename-me.py", "print 'hello'")
                .file("update-me.txt", "To be updated")
                .public_(true)
                .create();
    }

    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        gist.delete();
    }

    @Test
    public void testGitUpdater() throws Exception {
        GHGistUpdater updater = gist.update();
        GHGist updatedGist = updater.description("Description updated by API")
                .addFile("new-file.txt", "Added by updater")
                //.deleteFile("delete-me.txt")
                .renameFile("rename-me.py", "renamed.py")
                .updateFile("update-me.txt", "Content updated by API")
                .update();

        assertEquals("Description updated by API", updatedGist.getDescription());

        Map<String,GHGistFile> files = updatedGist.getFiles();

        // Check that the unmodified file stays intact.
        assertTrue(files.containsKey("unmodified.txt"));
        assertEquals("Should be unmodified", files.get("unmodified.txt").getContent());

        // Check that the files are updated as expected.
        //assertFalse("File was not deleted.", files.containsKey("delete-me.txt"));

        assertTrue(files.containsKey("new-file.txt"));
        assertEquals("Added by updater", files.get("new-file.txt").getContent());

        assertFalse(files.containsKey("rename-me.py"));
        assertTrue(files.containsKey("renamed.py"));
        assertEquals("print 'hello'", files.get("renamed.py").getContent());

        assertEquals("Content updated by API", files.get("update-me.txt").getContent());
    }
}

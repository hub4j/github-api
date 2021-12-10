package org.kohsuke.github;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHGistTest extends AbstractGitHubWireMockTest {
    @Test
    public void lifecycleTest() throws Exception {
        // CRUD operation
        GHGist gist = gitHub.createGist()
                .public_(false)
                .description("Test Gist")
                .file("abc.txt", "abc")
                .file("def.txt", "def")
                .file("ghi.txt", "ghi")
                .create();

        assertThat(gist.getCreatedAt(), is(notNullValue()));
        assertThat(gist.getDescription(), equalTo("Test Gist"));
        assertThat(gist.getFiles().size(), equalTo(3));

        assertThat(gist.getUpdatedAt(), notNullValue());
        assertThat(gist.getCommentsUrl(), notNullValue());
        assertThat(gist.getCommitsUrl(), notNullValue());
        assertThat(gist.getForksUrl(), notNullValue());
        assertThat(gist.getGitPullUrl(), notNullValue());
        assertThat(gist.getGitPushUrl(), notNullValue());
        assertThat(gist.getHtmlUrl(), notNullValue());
        assertThat(gist.getHtmlUrl(), notNullValue());

        String id = gist.getGistId();

        GHGist gistUpdate = gitHub.getGist(id);
        assertThat(gistUpdate.getGistId(), equalTo(gist.getGistId()));
        assertThat(gistUpdate.getDescription(), equalTo(gist.getDescription()));
        assertThat(gistUpdate.getFiles().size(), equalTo(3));

        gistUpdate = gistUpdate.update().description("Gist Test").addFile("jkl.txt", "jkl").update();

        assertThat(gistUpdate.getGistId(), equalTo(gist.getGistId()));
        assertThat(gistUpdate.getDescription(), equalTo("Gist Test"));
        assertThat(gistUpdate.getFiles().size(), equalTo(4));

        gistUpdate = gistUpdate.update()
                .renameFile("abc.txt", "ab.txt")
                .deleteFile("def.txt")
                .updateFile("ghi.txt", "gh")
                .updateFile("jkl.txt", "klm.txt", "nop")
                .update();

        assertThat(gistUpdate.getGistId(), equalTo(gist.getGistId()));
        assertThat(gistUpdate.getDescription(), equalTo("Gist Test"));
        assertThat(gistUpdate.getFiles().size(), equalTo(3));

        // verify delete works
        assertThat(gistUpdate.getFile("def.txt"), nullValue());

        // verify rename
        assertThat(gistUpdate.getFile("ab.txt").getContent(), equalTo("abc"));

        // verify updates
        assertThat(gistUpdate.getFile("ghi.txt").getContent(), equalTo("gh"));
        assertThat(gistUpdate.getFile("klm.txt").getContent(), equalTo("nop"));

        // rename and update on the same file in one update shoudl work.
        gistUpdate = gistUpdate.update().renameFile("ab.txt", "a.txt").updateFile("ab.txt", "abcd").update();

        assertThat(gistUpdate.getGistId(), equalTo(gist.getGistId()));
        assertThat(gistUpdate.getFiles().size(), equalTo(3));

        // verify rename and update
        assertThat(gistUpdate.getFile("a.txt").getContent(), equalTo("abcd"));

        try {
            gist.getId();
            fail("Newly created gists do not have numeric ids.");
        } catch (NumberFormatException e) {
            assertThat(e, notNullValue());
        }

        assertThat(gist.getGistId(), notNullValue());

        gist.delete();

        try {
            gitHub.getGist(id);
            fail("Gist should be deleted.");
        } catch (FileNotFoundException e) {
            assertThat(e, notNullValue());
        }
    }

    @Test
    public void starTest() throws Exception {
        GHGist gist = gitHub.getGist("9903708");
        assertThat(gist.getOwner().getLogin(), equalTo("rtyler"));

        // Random: test that comment count works
        assertThat(gist.getCommentCount(), equalTo(1));

        gist.star();
        assertThat(gist.isStarred(), is(true));

        gist.unstar();
        assertThat(gist.isStarred(), is(false));

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

        assertThat(gist.isPublic(), is(true));
        assertThat(gist.getId(), equalTo(9903708L));
        assertThat(gist.getGistId(), equalTo("9903708"));

        assertThat(gist.getFiles().size(), equalTo(1));
        GHGistFile f = gist.getFile("keybase.md");

        assertThat(f.getType(), equalTo("text/markdown"));
        assertThat(f.getLanguage(), equalTo("Markdown"));
        assertThat(f.getContent(), containsString("### Keybase proof"));
    }

    /**
     * Test file content of GHGistFile from listGists(). Test corresponding to
     * <a href="https://github.com/hub4j/github-api/issues/1325">issue #1325</a>.
     *
     * @throws Exception
     *             happens during the test run
     */
    @Test
    public void listGistFile() throws Exception {
        List<GHGist> gistList = getNonRecordingGitHub().getMyself().listGists().toList();

        assertThat(gistList.size(), equalTo(1));

        GHGist gist = gistList.get(0);

        assertThat(gist.isTruncated(), equalTo(false));
        assertThat(gist.getFiles().size(), equalTo(2));

        GHGistFile gistFileWithContent = gist.getFile("test_github_gist.md");
        GHGistFile emptyGistFile = gist.getFile("test_empty_github_gist.md");

        assertThat(gistFileWithContent.getFileName(), equalTo("test_github_gist.md"));
        // first time lazy download the file content and cache it
        assertThat(gistFileWithContent.getContent(), equalTo("Test github gist file content"));
        // second time directly load from cache
        assertThat(gistFileWithContent.getContent(), equalTo("Test github gist file content"));

        assertThat(emptyGistFile.getFileName(), equalTo("test_empty_github_gist.md"));
        assertThat(emptyGistFile.getContent(), equalTo(""));
    }
}

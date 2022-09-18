package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class GHGistFileTest extends AbstractGitHubWireMockTest {
    @Test
    public void truncateTest() throws Exception {
        char[] data = new char[1000000];
        String strLess1MB = new String(data);
        String strGreater1MB = strLess1MB + "abcde";
        // CRUD operation
        GHGist gist = gitHub.createGist()
                .public_(false)
                .description("Test Truncate")
                .file("greater1MB.txt", strGreater1MB)
                .create();

        // test truncate and content value
        assertThat(gist.getFile("greater1MB.txt").getContent(), equalTo(strLess1MB));

        String id = gist.getGistId();

        GHGist gistUpdate = gitHub.getGist(id);

        gistUpdate.update().description("Test Truncate").deleteFile("greater1MB.txt").update();
    }
}

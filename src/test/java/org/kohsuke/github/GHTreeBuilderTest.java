package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GHTreeBuilderTest extends AbstractGitHubWireMockTest {
    private static String REPO_NAME = "hub4j-test-org/GHTreeBuilderTest";

    private static String PATH_SCRIPT = "app/run.sh";
    private static String CONTENT_SCRIPT = "#!/bin/bash\necho Hello\n";

    private static String PATH_README = "doc/readme.txt";
    private static String CONTENT_README = "Thanks for using our application!\n";

    private static String PATH_DATA1 = "data/val1.dat";
    private static byte[] CONTENT_DATA1 = { 0x01, 0x02, 0x03 };

    private static String PATH_DATA2 = "data/val2.dat";
    private static byte[] CONTENT_DATA2 = { 0x04, 0x05, 0x06, 0x07 };

    private GHRepository repo;
    private GHRef masterRef;
    private GHTreeBuilder treeBuilder;

    @Before
    @After
    public void cleanup() throws Exception {
        if (mockGitHub.isUseProxy()) {
            repo = getGitHubBeforeAfter().getRepository(REPO_NAME);
            Arrays.asList(PATH_SCRIPT, PATH_README, PATH_DATA1, PATH_DATA2).forEach(path -> {
                try {
                    GHContent content = repo.getFileContent(path);
                    if (content != null) {
                        content.delete("Cleanup");
                    }
                } catch (IOException e) {
                }
            });
        }
    }

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(REPO_NAME);
        masterRef = repo.getRef("heads/master");
        String masterTreeSha = repo.getTreeRecursive("master", 1).getSha();
        treeBuilder = repo.createTree().baseTree(masterTreeSha);
    }

    @Test
    @Ignore("It seems that GitHub no longer supports the 'content' parameter")
    public void testTextEntry() throws Exception {
        treeBuilder.textEntry(PATH_SCRIPT, CONTENT_SCRIPT, true);
        treeBuilder.textEntry(PATH_README, CONTENT_README, false);

        updateTree();

        assertEquals(CONTENT_SCRIPT.length(), getFileSize(PATH_SCRIPT));
        assertEquals(CONTENT_README.length(), getFileSize(PATH_README));
    }

    @Test
    public void testShaEntry() throws Exception {
        String dataSha1 = new GHBlobBuilder(repo).binaryContent(CONTENT_DATA1).create().getSha();
        treeBuilder.shaEntry(PATH_DATA1, dataSha1, false);

        String dataSha2 = new GHBlobBuilder(repo).binaryContent(CONTENT_DATA2).create().getSha();
        treeBuilder.shaEntry(PATH_DATA2, dataSha2, false);

        updateTree();

        assertEquals(CONTENT_DATA1.length, getFileSize(PATH_DATA1));
        assertEquals(CONTENT_DATA2.length, getFileSize(PATH_DATA2));
    }

    @Test
    public void testAdd() throws Exception {
        treeBuilder.add(PATH_SCRIPT, CONTENT_SCRIPT, true);
        treeBuilder.add(PATH_README, CONTENT_README, false);
        treeBuilder.add(PATH_DATA1, CONTENT_DATA1, false);
        treeBuilder.add(PATH_DATA2, CONTENT_DATA2, false);

        updateTree();

        assertEquals(CONTENT_SCRIPT.length(), getFileSize(PATH_SCRIPT));
        assertEquals(CONTENT_README.length(), getFileSize(PATH_README));
        assertEquals(CONTENT_DATA1.length, getFileSize(PATH_DATA1));
        assertEquals(CONTENT_DATA2.length, getFileSize(PATH_DATA2));
    }

    private void updateTree() throws IOException {
        String treeSha = treeBuilder.create().getSha();
        String commitSha = new GHCommitBuilder(repo).message("Add files")
                .tree(treeSha)
                .parent(masterRef.getObject().getSha())
                .create()
                .getSHA1();
        masterRef.updateTo(commitSha);
    }

    private long getFileSize(String path) throws IOException {
        GHContent content = repo.getFileContent(path);
        if (content == null)
            throw new IOException("File not found: " + path);
        return content.getSize();
    }
}

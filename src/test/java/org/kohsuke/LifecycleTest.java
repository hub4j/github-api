package org.kohsuke;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

public class LifecycleTest extends TestCase {
    private GitHub gitHub;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gitHub = GitHub.connect();
    }

    public void testCreateRepository() throws IOException, GitAPIException {
        GHMyself myself = gitHub.getMyself();
        GHRepository repository = myself.getRepository("github-api-test");
        if (repository != null) {
            repository.delete();
        }
        repository = gitHub.createRepository("github-api-test",
                "a test repository used to test kohsuke's github-api", "http://github-api.kohsuke.org/", true);

        assertTrue(repository.getReleases().isEmpty());
        try {
            GHMilestone milestone = repository.createMilestone("Initial Release", "first one");
            GHIssue issue = repository.createIssue("Test Issue")
                    .body("issue body just for grins")
                    .milestone(milestone)
                    .assignee(myself)
                    .label("bug")
                    .create();
            File repoDir = new File(System.getProperty("java.io.tmpdir"), "github-api-test");
            delete(repoDir);
            Git origin = Git.cloneRepository()
                    .setBare(false)
                    .setURI(repository.gitHttpTransportUrl())
                    .setDirectory(repoDir)
                    .setCredentialsProvider(getCredentialsProvider(myself))
                    .call();

            commitTestFile(myself, repoDir, origin);


            GHRelease release = createRelease(repository);

            GHAsset asset = uploadAsset(release);

            updateAsset(release, asset);

            deleteAsset(release, asset);
        } finally {
            repository.delete();
        }
    }

    private void updateAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.setLabel("test label");
        assertEquals("test label", release.getAssets()[0].getLabel());
    }

    private void deleteAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.delete();
        assertEquals(0, release.getAssets().length);
    }

    private GHAsset uploadAsset(GHRelease release) throws IOException {
        GHAsset asset = release.uploadAsset(new File("pom.xml"), "application/text");
        assertNotNull(asset);
        GHAsset[] assets = release.getAssets();
        assertEquals(1, assets.length);
        assertEquals("pom.xml", assets[0].getName());

        return asset;
    }

    private GHRelease createRelease(GHRepository repository) throws IOException {
        GHRelease builder = repository.createRelease("release_tag")
                .name("Test Release")
                .body("How exciting!  To be able to programmatically create releases is a dream come true!")
                .create();
        List<GHRelease> releases = repository.getReleases();
        assertEquals(1, releases.size());
        GHRelease release = releases.get(0);
        assertEquals("Test Release", release.getName());
        return release;
    }

    private void commitTestFile(GHMyself myself, File repoDir, Git origin) throws IOException, GitAPIException {
        File dummyFile = createDummyFile(repoDir);
        DirCache cache = origin.add().addFilepattern(dummyFile.getName()).call();
        origin.commit().setMessage("test commit").call();
        origin.push().setCredentialsProvider(getCredentialsProvider(myself)).call();
    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider(GHMyself myself) throws IOException {
        Properties props = new Properties();
        File homeDir = new File(System.getProperty("user.home"));
        FileInputStream in = new FileInputStream(new File(homeDir, ".github"));
        try {
            props.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return new UsernamePasswordCredentialsProvider(props.getProperty("login"), props.getProperty("password"));
    }

    private void delete(File toDelete) {
        if (toDelete.isDirectory()) {
            for (File file : toDelete.listFiles()) {
                delete(file);
            }
        }
        toDelete.delete();
    }

    private File createDummyFile(File repoDir) throws IOException {
        File file = new File(repoDir, "testFile-" + System.currentTimeMillis());
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
            writer.println("test file");
        } finally {
            writer.close();
        }
        return file;
    }
}

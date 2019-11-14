package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

public class LifecycleTest extends AbstractGitHubApiTestBase {
    @Test
    public void testCreateRepository() throws IOException, GitAPIException, InterruptedException {
        GHMyself myself = gitHub.getMyself();
        GHOrganization org = gitHub.getOrganization("github-api-test-org");
        GHRepository repository = org.getRepository("github-api-test");
        if (repository != null) {
            repository.delete();
            Thread.sleep(1000);
        }
        repository = org.createRepository("github-api-test", "a test repository used to test kohsuke's github-api",
                "http://github-api.kohsuke.org/", "Core Developers", true);
        Thread.sleep(1000); // wait for the repository to become ready

        assertTrue(repository.getReleases().isEmpty());
        try {
            GHMilestone milestone = repository.createMilestone("Initial Release", "first one");
            GHIssue issue = repository.createIssue("Test Issue").body("issue body just for grins").milestone(milestone)
                    .assignee(myself).label("bug").create();
            File repoDir = new File(System.getProperty("java.io.tmpdir"), "github-api-test");
            delete(repoDir);
            Git origin = Git.cloneRepository().setBare(false).setURI(repository.getSshUrl()).setDirectory(repoDir)
                    .setCredentialsProvider(getCredentialsProvider(myself)).call();

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
        assertEquals("test label", release.getAssets().get(0).getLabel());
    }

    private void deleteAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.delete();
        assertEquals(0, release.getAssets().size());
    }

    private GHAsset uploadAsset(GHRelease release) throws IOException {
        GHAsset asset = release.uploadAsset(new File("pom.xml"), "application/text");
        assertNotNull(asset);
        List<GHAsset> assets = release.getAssets();
        assertEquals(1, assets.size());
        assertEquals("pom.xml", assets.get(0).getName());

        return asset;
    }

    private GHRelease createRelease(GHRepository repository) throws IOException {
        GHRelease builder = repository.createRelease("release_tag").name("Test Release")
                .body("How exciting!  To be able to programmatically create releases is a dream come true!").create();
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
        return new UsernamePasswordCredentialsProvider(props.getProperty("login"), props.getProperty("oauth"));
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

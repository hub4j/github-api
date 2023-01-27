package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * The Class GHDeployKeyTest.
 *
 * @author Jonas van Vliet
 */
public class GHDeployKeyTest extends AbstractGitHubWireMockTest {
    public static final String DEPLOY_KEY_TEST_REPO_NAME = "hub4j-test-org/GHDeployKeyTest";
    public static final String ED_25519_READONLY = "DeployKey - ed25519 - readonly";
    public static final String RSA_4096_READWRITE = "Deploykey - rsa4096 - readwrite";
    public static final String KEY_CREATOR_USERNAME = "van-vliet";

    /**
     * Test get deploymentkeys.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetDeployKeys() throws IOException {
        final GHRepository repo = getRepository();
        final List<GHDeployKey> deployKeys = repo.getDeployKeys();
        assertThat("There should be 2 deploykeys in " + DEPLOY_KEY_TEST_REPO_NAME, deployKeys, hasSize(2));

        Optional<GHDeployKey> ed25519Key = deployKeys.stream()
                .filter(key -> key.getTitle().equals(ED_25519_READONLY))
                .findAny();
        assertThat("The key exists", ed25519Key, isPresent());
        assertThat("The key was created at the specified date",
                ed25519Key.get().getCreatedAt(),
                is(Date.from(Instant.parse("2023-01-26T14:11:41.00Z"))));
        assertThat("The key is created by " + KEY_CREATOR_USERNAME,
                ed25519Key.get().getAdded_by(),
                is(KEY_CREATOR_USERNAME));
        assertThat("The key only has read access", ed25519Key.get().isRead_only(), is(true));

        Optional<GHDeployKey> rsa_4096Key = deployKeys.stream()
                .filter(key -> key.getTitle().equals(RSA_4096_READWRITE))
                .findAny();
        assertThat("The key exists", rsa_4096Key, isPresent());
        assertThat("The key was created at the specified date",
                rsa_4096Key.get().getCreatedAt(),
                is(Date.from(Instant.parse("2023-01-26T14:12:12.00Z"))));
        assertThat("The key is created by " + KEY_CREATOR_USERNAME,
                rsa_4096Key.get().getAdded_by(),
                is(KEY_CREATOR_USERNAME));
        assertThat("The key only has read/write access", rsa_4096Key.get().isRead_only(), is(false));
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(final GitHub gitHub) throws IOException {
        return gitHub.getRepository(DEPLOY_KEY_TEST_REPO_NAME);
    }
}

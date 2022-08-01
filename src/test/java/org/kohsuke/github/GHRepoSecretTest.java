package org.kohsuke.github;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.SecretBox;
import com.goterl.lazysodium.utils.Key;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.Matchers.*;

public class GHRepoSecretTest extends AbstractGitHubWireMockTest {

    static final String SECRET_NAME = "REPO_SECRET_TEST";

    private GHRepoSecret repoSecret;

    private GHRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = getRepository();
    }

    @Test
    public void testCreateRepoSecret() throws IOException, SodiumException {
        GHPublicKey publicKey = repository.getPublicKey();

        SodiumJava sodium = new SodiumJava();
        LazySodiumJava lazySodium = new LazySodiumJava(sodium);

        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String encrypted = lazySodium
                .cryptoSecretBoxEasy("Very secret stuff.", nonce, Key.fromPlainString(publicKey.getKey()));

        repository.createSecret(SECRET_NAME, encrypted, publicKey.getKeyId());

        repoSecret = repository.getSecret(SECRET_NAME);

        assertThat(repoSecret, notNullValue());
        assertThat(repoSecret.getName(), equalTo(SECRET_NAME));

        repository.deleteSecret(SECRET_NAME);
        try {
            GHRepoSecret secret = repository.getSecret(SECRET_NAME);
            assertThat(secret, nullValue());
        } catch (FileNotFoundException e) {
            repoSecret = null;
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("teste-runners").getRepository("teste-runners-app");
    }
}

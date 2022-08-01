package org.kohsuke.github;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.SecretBox;
import com.goterl.lazysodium.utils.Key;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GHSecretTest extends AbstractGitHubWireMockTest {

    static final String SECRET_NAME = "TEST";

    @Test
    public void testCreateRepoSecret() throws IOException, SodiumException {
        GHRepository repo = getRepository();
        GHPublicKey publicKey = repo.getPublicKey();

        SodiumJava sodium = new SodiumJava();
        LazySodiumJava lazySodium = new LazySodiumJava(sodium);

        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String encrypted = lazySodium.cryptoSecretBoxEasy("secret", nonce, Key.fromPlainString(publicKey.getKey()));

        repo.createSecret(SECRET_NAME, encrypted, publicKey.getKeyId());
    }

    @Test
    public void testGetRepoSecret() throws IOException {
        GHRepository repo = getRepository();
        GHRepoSecret secret = repo.getSecret(SECRET_NAME);

        assertThat(secret, notNullValue());
        assertThat(secret.getName(), equalTo(SECRET_NAME));
    }

    @Test
    public void testListRepoSecrets() throws IOException {
        GHRepository repo = getRepository();
        PagedIterable<GHRepoSecret> secrets = repo.listSecrets();
        assertThat(secrets, notNullValue());
    }

    @Test
    public void testCreateOrgSecret() throws IOException, SodiumException {
        GHOrganization org = getOrganization();
        GHPublicKey publicKey = org.getPublicKey();

        SodiumJava sodium = new SodiumJava();
        LazySodiumJava lazySodium = new LazySodiumJava(sodium);

        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String encrypted = lazySodium.cryptoSecretBoxEasy("secret", nonce, Key.fromPlainString(publicKey.getKey()));

        long[] repoIds = new long[1];
        repoIds[0] = getRepository().getId();

        org.createSecret(SECRET_NAME, encrypted, publicKey.getKeyId(), "selected", repoIds);
    }

    @Test
    public void testGetOrgSecret() throws IOException {
        GHOrganization org = getOrganization();
        GHOrgSecret secret = org.getSecret(SECRET_NAME);

        assertThat(secret, notNullValue());
        assertThat(secret.getName(), equalTo(SECRET_NAME));
    }

    @Test
    public void testListOrgSecrets() throws IOException {
        GHOrganization org = getOrganization();
        PagedIterable<GHOrgSecret> secrets = org.listSecrets();
        assertThat(secrets, notNullValue());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    protected GHOrganization getOrganization() throws IOException {
        return getOrganization(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("teste-runners").getRepository("teste-runners-app");
    }

    private GHOrganization getOrganization(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("teste-runners");
    }
}

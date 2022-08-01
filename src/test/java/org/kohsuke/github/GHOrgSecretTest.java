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

public class GHOrgSecretTest extends AbstractGitHubWireMockTest {

    static final String SECRET_NAME = "ORG_SECRET_TEST";

    private GHOrgSecret orgSecret;

    private GHOrganization organization;

    @Before
    public void setUp() throws Exception {
        organization = getOrganization();
    }

    @Test
    public void testCreateOrgSecret() throws IOException, SodiumException {
        GHPublicKey publicKey = organization.getPublicKey();

        SodiumJava sodium = new SodiumJava();
        LazySodiumJava lazySodium = new LazySodiumJava(sodium);

        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String encrypted = lazySodium
                .cryptoSecretBoxEasy("Very secret stuff.", nonce, Key.fromPlainString(publicKey.getKey()));

        long[] repoIds = new long[1];
        repoIds[0] = 123456789; // Random repository id

        organization.createSecret(SECRET_NAME, encrypted, publicKey.getKeyId(), "selected", repoIds);
        orgSecret = organization.getSecret(SECRET_NAME);

        assertThat(orgSecret, notNullValue());
        assertThat(orgSecret.getName(), equalTo(SECRET_NAME));
        assertThat(orgSecret.getVisibility(), equalTo("selected"));

        organization.createSecret(SECRET_NAME, encrypted, publicKey.getKeyId(), "all");
        orgSecret = organization.getSecret(SECRET_NAME);

        assertThat(orgSecret, notNullValue());
        assertThat(orgSecret.getName(), equalTo(SECRET_NAME));
        assertThat(orgSecret.getVisibility(), equalTo("all"));

        organization.deleteSecret(SECRET_NAME);
        try {
            orgSecret = organization.getSecret(SECRET_NAME);
            assertThat(orgSecret, nullValue());
        } catch (FileNotFoundException e) {
            orgSecret = null;
        }
    }

    protected GHOrganization getOrganization() throws IOException {
        return getOrganization(gitHub);
    }

    private GHOrganization getOrganization(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("teste-runners");
    }
}

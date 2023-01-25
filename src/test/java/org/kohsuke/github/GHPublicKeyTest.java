package org.kohsuke.github;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Class GHPublicKeyTest.
 *
 * @author Jonas van Vliet
 */
public class GHPublicKeyTest extends AbstractGitHubWireMockTest {
    public static final String TMP_KEY_NAME = "Temporary user key";

    /**
     * Test adding a public key to the user
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testAddPublicKey() throws Exception {
        GHMyself me = gitHub.getMyself();
        try {
            me.addPublicKey(TMP_KEY_NAME, createPublicKey());
        } finally {
            cleanupTemporaryKeys();
        }
    }

    @After
    public void cleanupTemporaryKeys() throws IOException {
        GHMyself me = gitHub.getMyself();
        List<GHKey> tmpGhKeys = me.getPublicKeys()
                .stream()
                .filter(key -> key.getTitle().equals(TMP_KEY_NAME))
                .collect(Collectors.toList());
        for (GHKey ghKey : tmpGhKeys) {
            ghKey.delete();
        }
    }

    private String createPublicKey() {
        JSch jsch = new JSch();
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            ByteArrayOutputStream publicKey = new ByteArrayOutputStream();
            keyPair.writePublicKey(publicKey, "Temporary key for testing");
            return new String(publicKey.toByteArray());
        } catch (JSchException e) {
            throw new IllegalStateException("Could not generate SSH key", e);
        }
    }
}

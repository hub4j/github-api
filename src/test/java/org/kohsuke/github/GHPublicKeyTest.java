package org.kohsuke.github;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * The Class GHPublicKeyTest.
 *
 * @author Jonas van Vliet
 */
public class GHPublicKeyTest extends AbstractGitHubWireMockTest {
    public static final String TMP_KEY_NAME = "Temporary user key";
    public static final String WIREMOCK_SSH_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDepW2/BSVFM2AfuGGsvi+vjQzC0EBD3R+/7PNEvP0/nvTWxiC/tthfvvCJR6TKrsprCir5tiJFm73gX+K18W0RKYpkyg8H6d1eZu3q/JOiGvoDPeN8Oe9hOGeeexw1WOiz7ESPHzZYXI981evzHAzxxn8zibr2EryopVNsXyoenw==";

    /**
     * Test adding a public key to the user
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testAddPublicKey() throws Exception {
        GHKey newPublicKey = null;
        try {
            GHMyself me = gitHub.getMyself();

            // This test adds a public key to the users profile. Since such a key grants access to everything the user
            // can access, the private key must be a secret that only the current user can access.
            //
            // The following if-statement ensures that the test does not add keys when using wiremocks, but creates
            // a unique key when run without wiremocks.
            //
            if (this.mockGitHub.isUseProxy()) {
                newPublicKey = me.addPublicKey(TMP_KEY_NAME, WIREMOCK_SSH_PUBLIC_KEY);
            } else {
                newPublicKey = me.addPublicKey(TMP_KEY_NAME, createPublicKey());
            }
        } finally {
            if (newPublicKey != null) {
                newPublicKey.delete();
            }
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

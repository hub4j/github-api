package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHVerificationReasonTest.
 *
 * @author Sourabh Sarvotham Parkala
 */
public class GHVerificationReasonTest extends AbstractGitHubWireMockTest {

    /**
     * Test expired key.
     *
     * @throws Exception
     *             the exception
     */
    // Issue 737
    @Test
    public void testExpiredKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f01");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.EXPIRED_KEY));
    }

    /**
     * Test not signing key.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testNotSigningKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f02");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.NOT_SIGNING_KEY));
    }

    /**
     * Test gpgverify error.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGpgverifyError() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f03");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.GPGVERIFY_ERROR));
    }

    /**
     * Test gpgverify unavailable.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGpgverifyUnavailable() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f04");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.GPGVERIFY_UNAVAILABLE));
    }

    /**
     * Test unsigned.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUnsigned() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f05");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.UNSIGNED));
    }

    /**
     * Test unknown signature type.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUnknownSignatureType() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f06");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNKNOWN_SIGNATURE_TYPE));
    }

    /**
     * Test no user.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testNoUser() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f07");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.NO_USER));
    }

    /**
     * Test unverified email.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUnverifiedEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f08");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNVERIFIED_EMAIL));
    }

    /**
     * Test bad email.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testBadEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f09");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.BAD_EMAIL));
    }

    /**
     * Test unknown key.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUnknownKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f10");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNKNOWN_KEY));
    }

    /**
     * Test malformed signature.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testMalformedSignature() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f11");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.MALFORMED_SIGNATURE));
    }

    /**
     * Test invalid.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testInvalid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f12");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.INVALID));
    }

    /**
     * Test valid.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testValid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f13");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(true));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.VALID));
        assertThat(commit.getCommitShortInfo().getVerification().getPayload(), notNullValue());
        assertThat(commit.getCommitShortInfo().getVerification().getSignature(), notNullValue());
    }
}

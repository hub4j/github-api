package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * @author Sourabh Sarvotham Parkala
 */
public class GHVerificationReasonTest extends AbstractGitHubWireMockTest {
    // Issue 737
    @Test
    public void testExpiredKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f01");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.EXPIRED_KEY,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testNotSigningKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f02");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.NOT_SIGNING_KEY,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testGpgverifyError() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f03");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.GPGVERIFY_ERROR,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testGpgverifyUnavailable() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f04");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.GPGVERIFY_UNAVAILABLE,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testUnsigned() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f05");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNSIGNED, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testUnknownSignatureType() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f06");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNKNOWN_SIGNATURE_TYPE,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testNoUser() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f07");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.NO_USER, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testUnverifiedEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f08");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNVERIFIED_EMAIL,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testBadEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f09");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.BAD_EMAIL, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testUnknownKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f10");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNKNOWN_KEY,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testMalformedSignature() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f11");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.MALFORMED_SIGNATURE,
                equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testInvalid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f12");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.INVALID, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
    }

    @Test
    public void testValid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f13");
        assertThat("Sourabh Parkala", equalTo(commit.getCommitShortInfo().getAuthor().getName()));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(true));
        assertThat(GHVerification.Reason.VALID, equalTo(commit.getCommitShortInfo().getVerification().getReason()));
        assertThat(commit.getCommitShortInfo().getVerification().getPayload(), notNullValue());
        assertThat(commit.getCommitShortInfo().getVerification().getSignature(), notNullValue());
    }
}

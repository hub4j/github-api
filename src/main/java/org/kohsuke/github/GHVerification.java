package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The commit/tag can be signed by user. This object holds the verification status. Whether the Commit/Tag is signed or
 * not.
 *
 * @see <a href="https://developer.github.com/v3/git/tags/#signature-verification-object">tags signature
 *      verificatiion</a>
 * @see <a href="https://developer.github.com/v3/git/commits/#signature-verification-object">commits signature
 *      verificatiion</a>
 *
 * @author Sourabh Sarvotham Parkala
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHVerification {
    private String signature, payload;
    private boolean verified;
    private Reason reason;

    /**
     * Indicates whether GitHub considers the signature in this commit to be verified.
     *
     * @return true if the signature is valid else returns false.
     */
    public boolean getVerified() {
        return verified;
    }

    /**
     * Gets reason for verification value.
     *
     * @return return reason of type {@link Reason}, such as "valid" or "unsigned". The possible values can be found in
     *         {@link Reason}}
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Gets signature used for the verification.
     *
     * @return null if not signed else encoded signature.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Gets the payload that was signed.
     *
     * @return null if not signed else encoded signature.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * The possible values for reason in verification object from github.
     *
     * @see <a href="https://developer.github.com/v3/repos/commits/#signature-verification-object">List of possible
     *      reason values</a>
     * @author Sourabh Sarvotham Parkala
     */
    public enum Reason {
        EXPIRED_KEY,
        NOT_SIGNING_KEY,
        GPGVERIFY_ERROR,
        GPGVERIFY_UNAVAILABLE,
        UNSIGNED,
        UNKNOWN_SIGNATURE_TYPE,
        NO_USER,
        UNVERIFIED_EMAIL,
        BAD_EMAIL,
        UNKNOWN_KEY,
        MALFORMED_SIGNATURE,
        INVALID,
        VALID
    }
}

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * The commit/tag can be signed by user. This object holds the verification status. Whether the Commit/Tag is signed or
 * not.
 *
 * @author Sourabh Sarvotham Parkala
 * @see <a href="https://developer.github.com/v3/git/tags/#signature-verification-object">tags signature
 *      verification</a>
 * @see <a href="https://developer.github.com/v3/git/commits/#signature-verification-object">commits signature
 *      verification</a>
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
    public boolean isVerified() {
        return verified;
    }

    /**
     * Gets reason for verification value.
     *
     * @return reason of type {@link Reason}, such as "valid" or "unsigned". The possible values can be found in
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
     * @author Sourabh Sarvotham Parkala
     * @see <a href="https://developer.github.com/v3/repos/commits/#signature-verification-object">List of possible
     *      reason values</a>
     */
    public enum Reason {

        /** Signing key expired. */
        EXPIRED_KEY,

        /** The usage flags for the key that signed this don't allow signing. */
        NOT_SIGNING_KEY,

        /** The GPG verification service misbehaved. */
        GPGVERIFY_ERROR,

        /** The GPG verification service is unavailable at the moment. */
        GPGVERIFY_UNAVAILABLE,

        /** Unsigned. */
        UNSIGNED,

        /** Unknown signature type. */
        UNKNOWN_SIGNATURE_TYPE,

        /** Email used for signing not known to GitHub. */
        NO_USER,

        /** Email used for signing unverified on GitHub. */
        UNVERIFIED_EMAIL,

        /** Invalid email used for signing. */
        BAD_EMAIL,

        /** Key used for signing not known to GitHub. */
        UNKNOWN_KEY,

        /** Malformed signature. */
        MALFORMED_SIGNATURE,

        /** Invalid signature. */
        INVALID,

        /** Valid signature and verified by GitHub. */
        VALID,

        /** The signing certificate or its chain could not be verified. */
        BAD_CERT,

        /** Malformed signature. (Returned by graphQL) */
        MALFORMED_SIG,

        /** Valid signature, though certificate revocation check failed. */
        OCSP_ERROR,

        /** Valid signature, pending certificate revocation checking. */
        OCSP_PENDING,

        /** One or more certificates in chain has been revoked. */
        OCSP_REVOKED
    }
}

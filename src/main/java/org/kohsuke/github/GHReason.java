package org.kohsuke.github;

/**
 * The possible values for reason in verification object from github.
 * 
 * @see <a href="https://developer.github.com/v3/repos/commits/#signature-verification-object">List of possible reason
 *      values</a>
 * @author Sourabh Sarvotham Parkala
 */
public enum GHReason {
    expired_key,
    not_signing_key,
    gpgverify_error,
    gpgverify_unavailable,
    unsigned,
    unknown_signature_type,
    no_user,
    unverified_email,
    bad_email,
    unknown_key,
    malformed_signature,
    invalid,
    valid
}
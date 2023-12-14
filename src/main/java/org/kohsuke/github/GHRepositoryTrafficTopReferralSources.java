package org.kohsuke.github;

/**
 * Top referral source object.
 */
public class GHRepositoryTrafficTopReferralSources extends GHRepositoryTrafficReferralBase {
    private String referrer;

    /**
     * Instantiates a new Gh repository traffic top referral sources.
     */
    GHRepositoryTrafficTopReferralSources() {
    }

    /**
     * Instantiates a new Gh repository traffic top referral sources.
     *
     * @param count
     *            the count
     * @param uniques
     *            the uniques
     * @param referrer
     *            the referrer
     */
    GHRepositoryTrafficTopReferralSources(int count, int uniques, String referrer) {
        super(count, uniques);
        this.referrer = referrer;
    }

    /**
     * Gets referrer.
     *
     * @return the referrer
     */
    public String getReferrer() {
        return this.referrer;
    }
}

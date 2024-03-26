package org.kohsuke.github;

/**
 * Base class for traffic referral objects.
 */
public class GHRepositoryTrafficReferralBase {
    private int count;
    private int uniques;

    /**
     * Instantiates a new Gh repository traffic referral base.
     */
    GHRepositoryTrafficReferralBase() {
    }

    /**
     * Instantiates a new Gh repository traffic referral base.
     *
     * @param count
     *            the count
     * @param uniques
     *            the uniques
     */
    GHRepositoryTrafficReferralBase(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Gets uniques.
     *
     * @return the uniques
     */
    public int getUniques() {
        return this.uniques;
    }
}

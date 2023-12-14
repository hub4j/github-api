package org.kohsuke.github;

/**
 * Top referral path object.
 */
public class GHRepositoryTrafficTopReferralPath extends GHRepositoryTrafficReferralBase {
    private String path;
    private String title;

    /**
     * Instantiates a new Gh repository traffic top referral path.
     */
    GHRepositoryTrafficTopReferralPath() {
    }

    /**
     * Instantiates a new Gh repository traffic top referral path.
     *
     * @param count
     *            the count
     * @param uniques
     *            the uniques
     * @param path
     *            the path
     * @param title
     *            the title
     */
    GHRepositoryTrafficTopReferralPath(int count, int uniques, String path, String title) {
        super(count, uniques);
        this.path = path;
        this.title = title;
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
}

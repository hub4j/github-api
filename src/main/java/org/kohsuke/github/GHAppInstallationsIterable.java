package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for GHAppInstallation listing.
 */
class GHAppInstallationsIterable extends PagedIterable<GHAppInstallation> {

    /** The Constant APP_INSTALLATIONS_URL. */
    public static final String APP_INSTALLATIONS_URL = "/user/installations";

    /**
     * Instantiates a new GH app installations iterable.
     *
     * @param root
     *            the root
     */
    public GHAppInstallationsIterable(GitHub root) {
        super(new PaginatedEndpoint<>(root.getClient(),
                root.createRequest().withUrlPath(APP_INSTALLATIONS_URL).build(),
                GHAppInstallationsPage.class,
                GHAppInstallation.class,
                null));
    }
}

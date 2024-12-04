package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The type Gh autolink builder.
 *
 * @see GHRepository#createAutolink()
 * @see GHAutolink
 */
public class GHAutolinkBuilder {

    private final GHRepository repo;
    private final Requester req;
    private String keyPrefix;
    private String urlTemplate;
    private Boolean isAlphanumeric;

    /**
     * Instantiates a new Gh autolink builder.
     *
     * @param repo
     *            the repo
     */
    GHAutolinkBuilder(GHRepository repo) {
        this.repo = repo;
        req = repo.root().createRequest();
    }

    /**
     * With key prefix gh autolink builder.
     *
     * @param keyPrefix
     *            the key prefix
     * @return the gh autolink builder
     */
    public GHAutolinkBuilder withKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
    }

    /**
     * With url template gh autolink builder.
     *
     * @param urlTemplate
     *            the url template
     * @return the gh autolink builder
     */
    public GHAutolinkBuilder withUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
        return this;
    }

    /**
     * With is alphanumeric gh autolink builder.
     *
     * @param isAlphanumeric
     *            the is alphanumeric
     * @return the gh autolink builder
     */
    public GHAutolinkBuilder withIsAlphanumeric(boolean isAlphanumeric) {
        this.isAlphanumeric = isAlphanumeric;
        return this;
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/autolinks", repo.getOwnerName(), repo.getName());
    }

    /**
     * Create gh autolink.
     *
     * @return the gh autolink
     * @throws IOException
     *             the io exception
     */
    public GHAutolink create() throws IOException {
        GHAutolink autolink = req.method("POST")
                .with("key_prefix", keyPrefix)
                .with("url_template", urlTemplate)
                .with("is_alphanumeric", isAlphanumeric)
                .withHeader("Accept", "application/vnd.github+json")
                .withUrlPath(getApiTail())
                .fetch(GHAutolink.class);

        return autolink.lateBind(repo);
    }

}

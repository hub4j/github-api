package org.kohsuke.github;

import java.io.IOException;

/**
 * Modifies {@link GHRelease}.
 *
 * @see GHRelease#update() GHRelease#update()
 */
public class GHReleaseUpdater {
    private final GHRelease base;
    private final Requester builder;

    GHReleaseUpdater(GHRelease base) {
        this.base = base;
        this.builder = base.root.createRequest();
    }

    /**
     * Tag gh release updater.
     *
     * @param tag
     *            the tag
     * @return the gh release updater
     */
    public GHReleaseUpdater tag(String tag) {
        builder.with("tag_name", tag);
        return this;
    }

    /**
     * Body gh release updater.
     *
     * @param body
     *            The release notes body.
     * @return the gh release updater
     */
    public GHReleaseUpdater body(String body) {
        builder.with("body", body);
        return this;
    }

    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or commit SHA.
     *
     * @param commitish
     *            Defaults to the repository’s default branch (usually "master"). Unused if the Git tag already exists.
     * @return the gh release updater
     */
    public GHReleaseUpdater commitish(String commitish) {
        builder.with("target_commitish", commitish);
        return this;
    }

    /**
     * Optional.
     *
     * @param draft
     *            {@code true} to create a draft (unpublished) release, {@code false} to create a published one. Default
     *            is {@code false}.
     * @return the gh release updater
     */
    public GHReleaseUpdater draft(boolean draft) {
        builder.with("draft", draft);
        return this;
    }

    /**
     * Name gh release updater.
     *
     * @param name
     *            the name of the release
     * @return the gh release updater
     */
    public GHReleaseUpdater name(String name) {
        builder.with("name", name);
        return this;
    }

    /**
     * Optional
     *
     * @param prerelease
     *            {@code true} to identify the release as a prerelease. {@code false} to identify the release as a full
     *            release. Default is {@code false}.
     * @return the gh release updater
     */
    public GHReleaseUpdater prerelease(boolean prerelease) {
        builder.with("prerelease", prerelease);
        return this;
    }

    /**
     * Update gh release.
     *
     * @return the gh release
     * @throws IOException
     *             the io exception
     */
    public GHRelease update() throws IOException {
        return builder.method("PATCH")
                .withUrlPath(base.owner.getApiTailUrl("releases/" + base.id))
                .fetch(GHRelease.class)
                .wrap(base.owner);
    }

}

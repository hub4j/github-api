package org.kohsuke.github;

import java.io.IOException;

/**
 * Builder pattern for creating a {@link GHRelease}
 *
 * @see GHRepository#createRelease(String)
 */
public class GHReleaseBuilder {
    private final GHRepository repo;
    private final Requester builder;

    public GHReleaseBuilder(GHRepository ghRepository, String tag) {
        this.repo = ghRepository;
        this.builder = new Requester(repo.root);
        builder.with("tag_name", tag);
    }

    /**
     * @param body The release notes body.
     */
    public GHReleaseBuilder body(String body) {
        if (body != null) {
            builder.with("body", body);
        }
        return this;
    }

    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or
     * commit SHA.
     *
     * @param commitish Defaults to the repository’s default branch (usually "master"). Unused if the Git tag
     *                  already exists.
     * @return
     */
    public GHReleaseBuilder commitish(String commitish) {
        if (commitish != null) {
            builder.with("target_commitish", commitish);
        }
        return this;
    }

    /**
     * Optional.
     *
     * @param draft {@code true} to create a draft (unpublished) release, {@code false} to create a published one.
     *                          Default is {@code false}.
     */
    public GHReleaseBuilder draft(boolean draft) {
        builder.with("draft", draft);
        return this;
    }

    /**
     * @param name the name of the release
     */
    public GHReleaseBuilder name(String name) {
        if (name != null) {
            builder.with("name", name);
        }
        return this;
    }

    /**
     * Optional
     *
     * @param prerelease {@code true} to identify the release as a prerelease. {@code false} to identify the release
     *                               as a full release. Default is {@code false}.
     */
    public GHReleaseBuilder prerelease(boolean prerelease) {
        builder.with("prerelease", prerelease);
        return this;
    }

    public GHRelease create() throws IOException {
        return builder.to(repo.getApiTailUrl("releases"), GHRelease.class).wrap(repo);
    }
}

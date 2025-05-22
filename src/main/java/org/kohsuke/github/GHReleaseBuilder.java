package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Builder pattern for creating a {@link GHRelease}.
 *
 * @see GHRepository#createRelease(String) GHRepository#createRelease(String)
 */
public class GHReleaseBuilder {
    private final GHRepository repo;
    private final Requester builder;

    /**
     * Instantiates a new Gh release builder.
     *
     * @param ghRepository
     *            the gh repository
     * @param tag
     *            the tag
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Acceptable risk")
    public GHReleaseBuilder(GHRepository ghRepository, String tag) {
        this.repo = ghRepository;
        this.builder = repo.root().createRequest().method("POST");
        builder.with("tag_name", tag);
    }

    /**
     * Body gh release builder.
     *
     * @param body
     *            The release notes body.
     * @return the gh release builder
     */
    public GHReleaseBuilder body(String body) {
        builder.with("body", body);
        return this;
    }

    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or commit SHA.
     *
     * @param commitish
     *            Defaults to the repository’s default branch (usually "main"). Unused if the Git tag already exists.
     * @return the gh release builder
     */
    public GHReleaseBuilder commitish(String commitish) {
        builder.with("target_commitish", commitish);
        return this;
    }

    /**
     * Optional.
     *
     * @param draft
     *            {@code true} to create a draft (unpublished) release, {@code false} to create a published one. Default
     *            is {@code false}.
     * @return the gh release builder
     */
    public GHReleaseBuilder draft(boolean draft) {
        builder.with("draft", draft);
        return this;
    }

    /**
     * Name gh release builder.
     *
     * @param name
     *            the name of the release
     * @return the gh release builder
     */
    public GHReleaseBuilder name(String name) {
        builder.with("name", name);
        return this;
    }

    /**
     * Optional.
     *
     * @param prerelease
     *            {@code true} to identify the release as a prerelease. {@code false} to identify the release as a full
     *            release. Default is {@code false}.
     * @return the gh release builder
     */
    public GHReleaseBuilder prerelease(boolean prerelease) {
        builder.with("prerelease", prerelease);
        return this;
    }

    /**
     * Optional.
     *
     * @param categoryName
     *            the category of the discussion to be created for the release. Category should already exist
     * @return the gh release builder
     */
    public GHReleaseBuilder categoryName(String categoryName) {
        builder.with("discussion_category_name", categoryName);
        return this;
    }

    /**
     * Optional.
     *
     * @param generateReleaseNotes
     *            {@code true} to instruct GitHub to generate release name and notes automatically. {@code false} to
     *            suppress automatic generation. Default is {@code false}.
     * @return the gh release builder
     */
    public GHReleaseBuilder generateReleaseNotes(boolean generateReleaseNotes) {
        builder.with("generate_release_notes", generateReleaseNotes);
        return this;
    }

    /**
     * Values for whether this release should be the latest.
     */
    public static enum MakeLatest {

        /** Make this the latest release */
        TRUE,
        /** Do not make this the latest release */
        FALSE,
        /** Latest release is determined by date and higher semantic version */
        LEGACY;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Optional.
     *
     * @param latest
     *            Whether to make this the latest release. Default is {@code TRUE}
     * @return the gh release builder
     */
    public GHReleaseBuilder makeLatest(MakeLatest latest) {
        builder.with("make_latest", latest);
        return this;
    }

    /**
     * Create gh release.
     *
     * @return the gh release
     * @throws IOException
     *             the io exception
     */
    public GHRelease create() throws IOException {
        return builder.withUrlPath(repo.getApiTailUrl("releases")).fetch(GHRelease.class).wrap(repo);
    }
}

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

// TODO: Auto-generated Javadoc
/**
 * Release in a github repository.
 *
 * @see GHRepository#listReleases() () GHRepository#listReleases()
 * @see GHRepository#createRelease(String) GHRepository#createRelease(String)
 */
public class GHRelease extends GHObject {

    /**
     * Wrap.
     *
     * @param releases
     *            the releases
     * @param owner
     *            the owner
     * @return the GH release[]
     */
    static GHRelease[] wrap(GHRelease[] releases, GHRepository owner) {
        for (GHRelease release : releases) {
            release.wrap(owner);
        }
        return releases;
    }

    private List<GHAsset> assets;

    private String assetsUrl;
    private String body;
    private String discussionUrl;
    private boolean draft;
    private String htmlUrl;
    private String name;
    private boolean prerelease;
    private String publishedAt;
    private String tagName;
    private String tarballUrl;
    private String targetCommitish;
    private String uploadUrl;
    private String zipballUrl;
    /** The owner. */
    GHRepository owner;

    /**
     * Create default GHRelease instance
     */
    public GHRelease() {
    }

    /**
     * Deletes this release.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(owner.getApiTailUrl("releases/" + getId())).send();
    }

    /**
     * Get the cached assets.
     *
     * @return the assets
     */
    public List<GHAsset> getAssets() {
        return Collections.unmodifiableList(assets);
    }

    /**
     * Gets assets url.
     *
     * @return the assets url
     */
    public String getAssetsUrl() {
        return assetsUrl;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets discussion url. Only present if a discussion relating to the release exists
     *
     * @return the discussion url
     */
    public String getDiscussionUrl() {
        return discussionUrl;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets published at.
     *
     * @return the published at
     */
    public Instant getPublishedAt() {
        return GitHubClient.parseInstant(publishedAt);
    }

    /**
     * Gets published at.
     *
     * @return the published at
     * @deprecated Use #getPublishedAt()
     */
    @Deprecated
    public Date getPublished_at() {
        return Date.from(getPublishedAt());
    }

    /**
     * Gets tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets tarball url.
     *
     * @return the tarball url
     */
    public String getTarballUrl() {
        return tarballUrl;
    }

    /**
     * Gets target commitish.
     *
     * @return the target commitish
     */
    public String getTargetCommitish() {
        return targetCommitish;
    }

    /**
     * Gets upload url.
     *
     * @return the upload url
     */
    public String getUploadUrl() {
        return uploadUrl;
    }

    /**
     * Gets zipball url.
     *
     * @return the zipball url
     */
    public String getZipballUrl() {
        return zipballUrl;
    }

    /**
     * Is draft boolean.
     *
     * @return the boolean
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * Is prerelease boolean.
     *
     * @return the boolean
     */
    public boolean isPrerelease() {
        return prerelease;
    }

    /**
     * Re-fetch the assets of this release.
     *
     * @return the assets iterable
     */
    public PagedIterable<GHAsset> listAssets() {
        Requester builder = owner.root().createRequest();
        return builder.withUrlPath(getApiTailUrl("assets")).toIterable(GHAsset[].class, item -> item.wrap(this));
    }

    /**
     * Updates this release via a builder.
     *
     * @return the gh release updater
     */
    public GHReleaseUpdater update() {
        return new GHReleaseUpdater(this);
    }

    /**
     * Because github relies on SNI (http://en.wikipedia.org/wiki/Server_Name_Indication) this method will only work on
     * Java 7 or greater. Options for fixing this for earlier JVMs can be found here
     * http://stackoverflow.com/questions/12361090/server-name-indication-sni-on-java but involve more complicated
     * handling of the HTTP requests to github's API.
     *
     * @param file
     *            the file
     * @param contentType
     *            the content type
     * @return the gh asset
     * @throws IOException
     *             the io exception
     */
    public GHAsset uploadAsset(File file, String contentType) throws IOException {
        FileInputStream s = new FileInputStream(file);
        try {
            return uploadAsset(file.getName(), s, contentType);
        } finally {
            s.close();
        }
    }

    /**
     * Upload asset gh asset.
     *
     * @param filename
     *            the filename
     * @param stream
     *            the stream
     * @param contentType
     *            the content type
     * @return the gh asset
     * @throws IOException
     *             the io exception
     */
    public GHAsset uploadAsset(String filename, InputStream stream, String contentType) throws IOException {
        Requester builder = owner.root().createRequest().method("POST");
        String url = getUploadUrl();
        // strip the helpful garbage from the url
        int endIndex = url.indexOf('{');
        if (endIndex != -1) {
            url = url.substring(0, endIndex);
        }
        url += "?name=" + URLEncoder.encode(filename, "UTF-8");
        return builder.contentType(contentType).with(stream).withUrlPath(url).fetch(GHAsset.class).wrap(this);
    }

    private String getApiTailUrl(String end) {
        return owner.getApiTailUrl(format("releases/%s/%s", getId(), end));
    }

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH release
     */
    GHRelease wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}

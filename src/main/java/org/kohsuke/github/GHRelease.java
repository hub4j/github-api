package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.String.*;

// TODO: Auto-generated Javadoc
/**
 * Release in a github repository.
 *
 * @see GHRepository#listReleases() () GHRepository#listReleases()
 * @see GHRepository#createRelease(String) GHRepository#createRelease(String)
 */
public class GHRelease extends GHObject {

    /** The owner. */
    GHRepository owner;

    private String html_url;
    private String assets_url;
    private List<GHAsset> assets;
    private String upload_url;
    private String tag_name;
    private String target_commitish;
    private String name;
    private String body;
    private boolean draft;
    private boolean prerelease;
    private Date published_at;
    private String tarball_url;
    private String zipball_url;
    private String discussion_url;

    /**
     * Gets discussion url. Only present if a discussion relating to the release exists
     *
     * @return the discussion url
     */
    public String getDiscussionUrl() {
        return discussion_url;
    }

    /**
     * Gets assets url.
     *
     * @return the assets url
     */
    public String getAssetsUrl() {
        return assets_url;
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
     * Is draft boolean.
     *
     * @return the boolean
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
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
     * Sets name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
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
     * Is prerelease boolean.
     *
     * @return the boolean
     */
    public boolean isPrerelease() {
        return prerelease;
    }

    /**
     * Gets published at.
     *
     * @return the published at
     */
    public Date getPublished_at() {
        return new Date(published_at.getTime());
    }

    /**
     * Gets tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
        return tag_name;
    }

    /**
     * Gets target commitish.
     *
     * @return the target commitish
     */
    public String getTargetCommitish() {
        return target_commitish;
    }

    /**
     * Gets upload url.
     *
     * @return the upload url
     */
    public String getUploadUrl() {
        return upload_url;
    }

    /**
     * Gets zipball url.
     *
     * @return the zipball url
     */
    public String getZipballUrl() {
        return zipball_url;
    }

    /**
     * Gets tarball url.
     *
     * @return the tarball url
     */
    public String getTarballUrl() {
        return tarball_url;
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

    /**
     * Get the cached assets.
     *
     * @return the assets
     */
    public List<GHAsset> getAssets() {
        return Collections.unmodifiableList(assets);
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
     * Deletes this release.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(owner.getApiTailUrl("releases/" + getId())).send();
    }

    /**
     * Updates this release via a builder.
     *
     * @return the gh release updater
     */
    public GHReleaseUpdater update() {
        return new GHReleaseUpdater(this);
    }

    private String getApiTailUrl(String end) {
        return owner.getApiTailUrl(format("releases/%s/%s", getId(), end));
    }
}

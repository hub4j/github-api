package org.kohsuke.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.*;

/**
 * Release in a github repository.
 *
 * @see GHRepository#getReleases() GHRepository#getReleases()
 * @see GHRepository#createRelease(String) GHRepository#createRelease(String)
 */
public class GHRelease extends GHObject {
    GitHub root;
    GHRepository owner;

    private String html_url;
    private String assets_url;
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
     * Sets draft.
     *
     * @param draft
     *            the draft
     * @return the draft
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #update()}
     */
    public GHRelease setDraft(boolean draft) throws IOException {
        return update().draft(draft).update();
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
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
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Sets owner.
     *
     * @param owner
     *            the owner
     */
    public void setOwner(GHRepository owner) {
        this.owner = owner;
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
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
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

    GHRelease wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

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
        Requester builder = owner.root.retrieve().method("POST");
        String url = getUploadUrl();
        // strip the helpful garbage from the url
        url = url.substring(0, url.indexOf('{'));
        url += "?name=" + URLEncoder.encode(filename, "UTF-8");
        return builder.contentType(contentType).with(stream).to(url, GHAsset.class).wrap(this);
    }

    /**
     * Gets assets.
     *
     * @return the assets
     * @throws IOException
     *             the io exception
     */
    public List<GHAsset> getAssets() throws IOException {
        Requester builder = owner.root.retrieve().method("POST");

        GHAsset[] assets = builder.method("GET").toArray(getApiTailUrl("assets"), GHAsset[].class);
        return Arrays.asList(GHAsset.wrap(assets, this));
    }

    /**
     * Deletes this release.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").to(owner.getApiTailUrl("releases/" + id));
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
        return owner.getApiTailUrl(format("releases/%s/%s", id, end));
    }
}

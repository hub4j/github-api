package org.kohsuke.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

/**
 * Release in a github repository.
 *
 * @see GHRepository#getReleases()
 * @see GHRepository#createRelease(String)
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

    public String getAssetsUrl() {
        return assets_url;
    }

    public void setAssetsUrl(String assets_url) {
        this.assets_url = assets_url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public String getHtmlUrl() {
        return html_url;
    }

    public void setHtmlUrl(String html_url) {
        this.html_url = html_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public void setOwner(GHRepository owner) {
        this.owner = owner;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public Date getPublished_at() {
        return published_at;
    }

    public void setPublished_at(Date published_at) {
        this.published_at = published_at;
    }

    public GitHub getRoot() {
        return root;
    }

    public void setRoot(GitHub root) {
        this.root = root;
    }

    public String getTagName() {
        return tag_name;
    }

    public void setTagName(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTargetCommitish() {
        return target_commitish;
    }

    public void setTargetCommitish(String target_commitish) {
        this.target_commitish = target_commitish;
    }

    public String getUploadUrl() {
        return upload_url;
    }

    public void setUploadUrl(String upload_url) {
        this.upload_url = upload_url;
    }

    public String getZipballUrl() {
        return zipball_url;
    }

    public void setZipballUrl(String zipballUrl) {
        this.zipball_url = zipballUrl;
    }

    public String getTarballUrl() {
        return tarball_url;
    }

    public void setTarballUrl(String tarballUrl) {
        this.tarball_url = tarballUrl;
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
     * Java 7 or greater.  Options for fixing this for earlier JVMs can be found here
     * http://stackoverflow.com/questions/12361090/server-name-indication-sni-on-java but involve more complicated
     * handling of the HTTP requests to github's API.
     *
     * @throws IOException
     */
    public GHAsset uploadAsset(File file, String contentType) throws IOException {
        Requester builder = new Requester(owner.root);

        String url = format("https://uploads.github.com%sreleases/%d/assets?name=%s",
                owner.getApiTailUrl(""), getId(), file.getName());
        return builder.contentType(contentType)
                .with(new FileInputStream(file))
                .to(url, GHAsset.class).wrap(this);
    }

    public List<GHAsset> getAssets() throws IOException {
        Requester builder = new Requester(owner.root);

        GHAsset[] assets = builder
                .method("GET")
                .to(getApiTailUrl("assets"), GHAsset[].class);
        return Arrays.asList(GHAsset.wrap(assets, this));
    }

    /**
     * Deletes this release.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(owner.getApiTailUrl("releases/"+id));
    }

    private String getApiTailUrl(String end) {
        return owner.getApiTailUrl(format("releases/%s/%s",id,end));
    }
}

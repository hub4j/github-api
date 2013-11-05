package org.kohsuke.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static java.lang.String.format;

public class GHRelease {
    GitHub root;
    GHRepository owner;

    private String url;
    private String html_url;
    private String assets_url;
    private String upload_url;
    private long id;
    private String tag_name;
    private String target_commitish;
    private String name;
    private String body;
    private boolean draft;
    private boolean prerelease;
    private Date created_at;
    private Date published_at;

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

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public GHAsset[] getAssets() throws IOException {
        Requester builder = new Requester(owner.root);

        GHAsset[] assets = (GHAsset[]) builder
                .method("GET")
                .to(owner.getApiTailUrl(format("releases/%d/assets", id)), GHAsset[].class);
        return GHAsset.wrap(assets, this);
    }
}

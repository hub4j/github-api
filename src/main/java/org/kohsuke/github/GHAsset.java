package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Asset in a release.
 *
 * @see GHRelease#getAssets()
 */
public class GHAsset {
    GitHub root;
    GHRepository owner;
    private String url;
    private String id;
    private String name;
    private String label;
    private String state;
    private String content_type;
    private long size;
    private long download_count;
    private Date created_at;
    private Date updated_at;
    private String browser_download_url;

    public String getContentType() {
        return content_type;
    }

    public void setContentType(String contentType) throws IOException {
        edit("content_type", contentType);
        this.content_type = contentType;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public long getDownloadCount() {
        return download_count;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) throws IOException {
        edit("label", label);
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public long getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public String getUrl() {
        return url;
    }

    public String getBrowserDownloadUrl() {
        return browser_download_url;
    }

    public void setBrowserDownloadUrl(String browser_download_url) {
        this.browser_download_url = browser_download_url;
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getApiRoute());
    }

    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(getApiRoute());
    }


    private String getApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/releases/assets/" + id;
    }

    GHAsset wrap(GHRelease release) {
        this.owner = release.getOwner();
        this.root = owner.root;
        return this;
    }

    public static GHAsset[] wrap(GHAsset[] assets, GHRelease release) {
        for (GHAsset aTo : assets) {
            aTo.wrap(release);
        }
        return assets;
    }
}

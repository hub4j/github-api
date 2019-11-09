package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * Asset in a release.
 *
 * @see GHRelease#getAssets()
 */
public class GHAsset extends GHObject {
    GHRepository owner;
    private String name;
    private String label;
    private String state;
    private String content_type;
    private long size;
    private long download_count;
    private String browser_download_url;

    public String getContentType() {
        return content_type;
    }

    public void setContentType(String contentType) throws IOException {
        edit("content_type", contentType);
        this.content_type = contentType;
    }

    public long getDownloadCount() {
        return download_count;
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

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public String getBrowserDownloadUrl() {
        return browser_download_url;
    }

    private void edit(String key, Object value) throws IOException {
        root.createRequester().with(key, value).method("PATCH").to(getApiRoute());
    }

    public void delete() throws IOException {
        root.createRequester().method("DELETE").to(getApiRoute());
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

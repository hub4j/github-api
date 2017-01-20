package org.kohsuke.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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

    public String getBody() {
        return body;
    }

    public void setTag_name(String tag_name) {
      this.tag_name = tag_name;
    }

    public void setTarget_commitish(String target_commitish) {
      this.target_commitish = target_commitish;
    }

    public void setBody(String body) {
      this.body = body;
    }

    public void setPrerelease(boolean prerelease) {
      this.prerelease = prerelease;
    }

    public boolean isDraft() {
        return draft;
    }

    public GHRelease setDraft(boolean draft) throws IOException {
      return setDraft(draft, true);
    }
    
    public GHRelease setDraft(boolean draft, boolean edit) throws IOException {
      if (edit) {
        edit("draft", draft);
      }
      this.draft = draft;
      return this;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
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

    public Date getPublished_at() {
        return new Date(published_at.getTime());
    }

    public GitHub getRoot() {
        return root;
    }

    public String getTagName() {
        return tag_name;
    }

    public String getTargetCommitish() {
        return target_commitish;
    }

    public String getUploadUrl() {
        return upload_url;
    }

    public String getZipballUrl() {
        return zipball_url;
    }

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
     * Java 7 or greater.  Options for fixing this for earlier JVMs can be found here
     * http://stackoverflow.com/questions/12361090/server-name-indication-sni-on-java but involve more complicated
     * handling of the HTTP requests to github's API.
     *
     * @throws IOException
     */
    public GHAsset uploadAsset(File file, String contentType) throws IOException {
        Requester builder = new Requester(owner.root);

        String url = format("https://uploads.github.com%s/releases/%d/assets?name=%s",
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
    
    /**
     * call to edit all editable fields
     */
    public void update() throws IOException {
      new Requester(root)
        // these are the editable fields
        ._with("tag_name", tag_name)
        ._with("target_commitish", target_commitish)
        ._with("name", name)
        ._with("body", body)
        ._with("draft", draft)
        ._with("prerelease", prerelease)
        .method("PATCH").to(owner.getApiTailUrl("releases/"+id));
    }

    /**
     * Edit this release.
     */
    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(owner.getApiTailUrl("releases/"+id));
    }

    private String getApiTailUrl(String end) {
        return owner.getApiTailUrl(format("releases/%s/%s",id,end));
    }
}

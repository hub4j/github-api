package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Gist
 *
 * @author Kohsuke Kawaguchi
 * @see GHUser#listGists()
 * @see GitHub#getGist(String)
 * @see GitHub#createGist()
 * @see <a href="https://developer.github.com/v3/gists/">documentation</a>
 */
public class GHGist extends GHObject {

    GHUser owner;

    private String forks_url, commits_url, id, git_pull_url, git_push_url, html_url;

    @JsonProperty("public")
    private boolean _public;

    private String description;

    private int comments;

    private String comments_url;

    private Map<String,GHGistFile> files = new HashMap<String, GHGistFile>();

    /**
     * User that owns this Gist.
     */
    @JacksonInject(value = "owner")
    @JsonProperty
    public GHUser getOwner() throws IOException {
        return getRoot().intern(owner);
    }
    public void setOwner(GHUser owner) {
        this.owner = getRoot().getUser(owner);
    }


    public String getForksUrl() {
        return forks_url;
    }

    public String getCommitsUrl() {
        return commits_url;
    }

    /**
     * URL like https://gist.github.com/gists/12345.git
     */
    public String getGitPullUrl() {
        return git_pull_url;
    }

    public String getGitPushUrl() {
        return git_push_url;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    public boolean isPublic() {
        return _public;
    }

    public String getDescription() {
        return description;
    }

    public int getCommentCount() {
        return comments;
    }

    /**
     * API URL of listing comments.
     */
    public String getCommentsUrl() {
        return comments_url;
    }

    public GHGistFile getFile(String name) {
        return files.get(name);
    }

    public Map<String,GHGistFile> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    @JsonSetter
    void setFiles(Map<String,GHGistFile> files) {
        for (Entry<String, GHGistFile> e : files.entrySet()) {
            e.getValue().fileName = e.getKey();
        }
        this.files.putAll(files);
    }

    /*package*/ GHGist wrapUp(GHUser owner) {
        return this;
    }

    String getApiTailUrl(String tail) {
        String result = "/gists/" + id;
        if (!StringUtils.isBlank(tail)) {
            result += StringUtils.prependIfMissing(tail, "/");
        }
        return result;
    }

    public void star() throws IOException {
        createRequest().method("PUT").to(getApiTailUrl("star"));
    }

    public void unstar() throws IOException {
        createRequest().method("DELETE").to(getApiTailUrl("star"));
    }

    public boolean isStarred() throws IOException {
        return getRoot().createRequest().method("GET").asHttpStatusCode(getApiTailUrl("star"))/100==2;
    }

    /**
     * Forks this gist into your own.
     */
    public GHGist fork() throws IOException {
        return createRequest().to(getApiTailUrl("forks"),GHGist.class);
    }

    public PagedIterable<GHGist> listForks() {
        return getRoot().createRequest().method("GET")
            .asPagedIterable(
                getApiTailUrl("forks"),
                GHGist[].class,
                null);
    }

    /**
     * Deletes this gist.
     */
    public void delete() throws IOException {
        createRequest().method("DELETE").to("/gists/" + id);
    }

    /**
     * Updates this gist via a builder.
     */
    public GHGistUpdater update() throws IOException {
        return new GHGistUpdater(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GHGist ghGist = (GHGist) o;
        return id.equals(ghGist.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}

package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    /* package almost final */ GHUser owner;
    /* package almost final */ GitHub root;

    private String forks_url, commits_url, id, git_pull_url, git_push_url, html_url;

    @JsonProperty("public")
    private boolean _public;

    private String description;

    private int comments;

    private String comments_url;

    private Map<String, GHGistFile> files = new HashMap<String, GHGistFile>();

    /**
     * @return User that owns this Gist.
     */
    public GHUser getOwner() throws IOException {
        return root.intern(owner);
    }

    public String getForksUrl() {
        return forks_url;
    }

    public String getCommitsUrl() {
        return commits_url;
    }

    /**
     * @return URL like https://gist.github.com/gists/12345.git
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
     * @return API URL of listing comments.
     */
    public String getCommentsUrl() {
        return comments_url;
    }

    public GHGistFile getFile(String name) {
        return files.get(name);
    }

    public Map<String, GHGistFile> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    GHGist wrapUp(GHUser owner) {
        this.owner = owner;
        this.root = owner.root;
        wrapUp();
        return this;
    }

    /**
     * Used when caller obtains {@link GHGist} without knowing its owner. A partially constructed owner object is
     * interned.
     */
    GHGist wrapUp(GitHub root) {
        this.owner = root.getUser(owner);
        this.root = root;
        wrapUp();
        return this;
    }

    private void wrapUp() {
        for (Entry<String, GHGistFile> e : files.entrySet()) {
            e.getValue().fileName = e.getKey();
        }
    }

    String getApiTailUrl(String tail) {
        String result = "/gists/" + id;
        if (!StringUtils.isBlank(tail)) {
            result += StringUtils.prependIfMissing(tail, "/");
        }
        return result;
    }

    public void star() throws IOException {
        new Requester(root).method("PUT").to(getApiTailUrl("star"));
    }

    public void unstar() throws IOException {
        new Requester(root).method("DELETE").to(getApiTailUrl("star"));
    }

    public boolean isStarred() throws IOException {
        return root.retrieve().asHttpStatusCode(getApiTailUrl("star")) / 100 == 2;
    }

    /**
     * Forks this gist into your own.
     */
    public GHGist fork() throws IOException {
        return new Requester(root).to(getApiTailUrl("forks"), GHGist.class).wrapUp(root);
    }

    public PagedIterable<GHGist> listForks() {
        return root.retrieve().asPagedIterable(getApiTailUrl("forks"), GHGist[].class, item -> item.wrapUp(root));
    }

    /**
     * Deletes this gist.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").to("/gists/" + id);
    }

    /**
     * Updates this gist via a builder.
     */
    public GHGistUpdater update() throws IOException {
        return new GHGistUpdater(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GHGist ghGist = (GHGist) o;
        return id.equals(ghGist.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    GHGist wrap(GHUser owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }
}

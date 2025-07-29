package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

// TODO: Auto-generated Javadoc
/**
 * Gist.
 *
 * @author Kohsuke Kawaguchi
 * @see GHUser#listGists() GHUser#listGists()
 * @see GitHub#getGist(String) GitHub#getGist(String)
 * @see GitHub#createGist() GitHub#createGist()
 * @see <a href="https://developer.github.com/v3/gists/">documentation</a>
 */
public class GHGist extends GHObject {

    private int comments;

    private String commentsUrl;

    private String description;

    private final Map<String, GHGistFile> files;

    private String forksUrl, commitsUrl, id, gitPullUrl, gitPushUrl, htmlUrl;

    @JsonProperty("public")
    private boolean isPublic;

    /** The owner. */
    final GHUser owner;

    @JsonCreator
    private GHGist(@JsonProperty("owner") GHUser owner, @JsonProperty("files") Map<String, GHGistFile> files) {
        for (Entry<String, GHGistFile> e : files.entrySet()) {
            e.getValue().fileName = e.getKey();
        }
        this.files = Collections.unmodifiableMap(files);
        this.owner = owner.root().getUser(owner);
    }

    /**
     * Deletes this gist.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath("/gists/" + id).send();
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GHGist ghGist = (GHGist) o;
        return id.equals(ghGist.id);

    }

    /**
     * Forks this gist into your own.
     *
     * @return the gh gist
     * @throws IOException
     *             the io exception
     */
    public GHGist fork() throws IOException {
        return root().createRequest().method("POST").withUrlPath(getApiTailUrl("forks")).fetch(GHGist.class);
    }

    /**
     * Gets comment count.
     *
     * @return the comment count
     */
    public int getCommentCount() {
        return comments;
    }

    /**
     * Gets comments url.
     *
     * @return API URL of listing comments.
     */
    public String getCommentsUrl() {
        return commentsUrl;
    }

    /**
     * Gets commits url.
     *
     * @return the commits url
     */
    public String getCommitsUrl() {
        return commitsUrl;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets file.
     *
     * @param name
     *            the name
     * @return the file
     */
    public GHGistFile getFile(String name) {
        return files.get(name);
    }

    /**
     * Gets files.
     *
     * @return the files
     */
    public Map<String, GHGistFile> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    /**
     * Gets forks url.
     *
     * @return the forks url
     */
    public String getForksUrl() {
        return forksUrl;
    }

    /**
     * Gets the id for this Gist. Unlike most other GitHub objects, the id for Gists can be non-numeric, such as
     * "aa5a315d61ae9438b18d". This should be used instead of {@link #getId()}.
     *
     * @return id of this Gist
     */
    public String getGistId() {
        return this.id;
    }

    /**
     * Gets git pull url.
     *
     * @return URL like https://gist.github.com/gists/12345.git
     */
    public String getGitPullUrl() {
        return gitPullUrl;
    }

    /**
     * Gets git push url.
     *
     * @return the git push url
     */
    public String getGitPushUrl() {
        return gitPushUrl;
    }

    /**
     * Get the html url.
     *
     * @return the github html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Unlike most other GitHub objects, the id for Gists can be non-numeric, such as "aa5a315d61ae9438b18d". If the id
     * is numeric, this method will get it. If id is not numeric, this will throw a runtime
     * {@link NumberFormatException}.
     *
     * @return id of the Gist.
     * @deprecated Use {@link #getGistId()} instead.
     */
    @Deprecated
    @Override
    public long getId() {
        return Long.parseLong(getGistId());
    }

    /**
     * Gets owner.
     *
     * @return User that owns this Gist.
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getOwner() {
        return owner;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Is public boolean.
     *
     * @return the boolean
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Is starred boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean isStarred() throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("star")).fetchHttpStatusCode() / 100 == 2;
    }

    /**
     * List forks paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHGist> listForks() {
        return root().createRequest().withUrlPath(getApiTailUrl("forks")).toIterable(GHGist[].class, null);
    }

    /**
     * Star.
     *
     * @throws IOException
     *             the io exception
     */
    public void star() throws IOException {
        root().createRequest().method("PUT").withUrlPath(getApiTailUrl("star")).send();
    }

    /**
     * Unstar.
     *
     * @throws IOException
     *             the io exception
     */
    public void unstar() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiTailUrl("star")).send();
    }

    /**
     * Updates this gist via a builder.
     *
     * @return the gh gist updater
     */
    public GHGistUpdater update() {
        return new GHGistUpdater(this);
    }

    /**
     * Gets the api tail url.
     *
     * @param tail
     *            the tail
     * @return the api tail url
     */
    String getApiTailUrl(String tail) {
        String result = "/gists/" + id;
        if (!StringUtils.isBlank(tail)) {
            result += StringUtils.prependIfMissing(tail, "/");
        }
        return result;
    }
}

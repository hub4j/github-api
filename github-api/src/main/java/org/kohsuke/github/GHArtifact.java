package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.function.InputStreamFunction;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO: Auto-generated Javadoc
/**
 * An artifact from a workflow run.
 *
 * @author Guillaume Smet
 */
public class GHArtifact extends GHObject {

    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;

    private String name;
    private long sizeInBytes;
    private String archiveDownloadUrl;
    private boolean expired;
    private String expiresAt;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size of the artifact in bytes.
     *
     * @return the size
     */
    public long getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * Gets the archive download URL.
     *
     * @return the archive download URL
     */
    public URL getArchiveDownloadUrl() {
        return GitHubClient.parseURL(archiveDownloadUrl);
    }

    /**
     * If this artifact has expired.
     *
     * @return if the artifact has expired
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * Gets the date at which this artifact will expire.
     *
     * @return the date of expiration
     */
    public Date getExpiresAt() {
        return GitHubClient.parseDate(expiresAt);
    }

    /**
     * Repository to which the artifact belongs.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() throws IOException {
        return null;
    }

    /**
     * Deletes the artifact.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Downloads the artifact.
     *
     * @param <T>
     *            the type of result
     * @param streamFunction
     *            The {@link InputStreamFunction} that will process the stream
     * @return the result of reading the stream.
     * @throws IOException
     *             The IO exception.
     */
    public <T> T download(InputStreamFunction<T> streamFunction) throws IOException {
        requireNonNull(streamFunction, "Stream function must not be null");

        return root().createRequest().method("GET").withUrlPath(getApiRoute(), "zip").fetchStream(streamFunction);
    }

    private String getApiRoute() {
        if (owner == null) {
            // Workflow runs returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");
        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/actions/artifacts/" + getId();
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH artifact
     */
    GHArtifact wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

}

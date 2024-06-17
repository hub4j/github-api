package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.net.URL;

/**
 * Represents an error from GitHub.
 *
 * @author Miguel Esteban Gutiérrez
 */
public class GHError implements Serializable {

    /**
     * The serial version UID of the error
     */
    private static final long serialVersionUID = 2008071901;

    /**
     * The error message.
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String message;

    /**
     * The URL to the documentation for the error.
     */
    @JsonProperty("documentation_url")
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String documentation;

    /**
     * Get the error message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the URL to the documentation for the error.
     *
     * @return the url
     */
    public URL getDocumentationUrl() {
        return GitHubClient.parseURL(documentation);
    }

}

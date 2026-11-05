package org.kohsuke.github.internal.graphql.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * A single error entry returned by a GitHub GraphQL response.
 *
 * <p>
 * Per the GraphQL specification, only {@code message} is guaranteed to be present. Other fields ({@code type},
 * {@code path}, {@code locations}, {@code extensions}) are optional and may be {@code null} depending on the error.
 * Unknown fields are ignored to remain forward-compatible with future server-side additions.
 * </p>
 *
 * @see <a href="https://spec.graphql.org/October2021/#sec-Errors">GraphQL spec — Errors</a>
 * @see <a href="https://docs.github.com/en/graphql/reference/objects#error">GitHub GraphQL — Error</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
        justification = "Populated via Jackson deserialization")
public class GHGraphQLError {

    /**
     * Source location of an error inside the GraphQL document.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "Populated via Jackson deserialization")
    public static class Location {

        private int column;

        private int line;

        /**
         * Default constructor used by Jackson.
         */
        public Location() {
        }

        /**
         * Get the column index of the location, starting at 1.
         *
         * @return the column index
         */
        public int getColumn() {
            return column;
        }

        /**
         * Get the line index of the location, starting at 1.
         *
         * @return the line index
         */
        public int getLine() {
            return line;
        }
    }

    private Map<String, Object> extensions;

    private List<Location> locations;

    private String message;

    private List<Object> path;

    private String type;

    /**
     * Default constructor used by Jackson.
     */
    public GHGraphQLError() {
    }

    /**
     * Get the extensions object as defined by the GraphQL spec. GitHub may include custom keys here, e.g. error code or
     * documentation URL. May be {@code null} if not provided.
     *
     * @return the extensions map, or {@code null}
     */
    @CheckForNull
    public Map<String, Object> getExtensions() {
        return extensions == null ? null : Collections.unmodifiableMap(extensions);
    }

    /**
     * Get the source locations associated with this error. Each entry points to a line/column in the GraphQL document.
     * May be {@code null} if not provided.
     *
     * @return the list of locations, or {@code null}
     */
    @CheckForNull
    public List<Location> getLocations() {
        return locations == null ? null : Collections.unmodifiableList(locations);
    }

    /**
     * Get the human-readable error message. Per the GraphQL spec this field is always present.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the response path that failed. Each segment is either a {@link String} field name or an {@link Integer} list
     * index, per the GraphQL spec. May be {@code null} if not provided.
     *
     * @return the path elements, or {@code null}
     */
    @CheckForNull
    public List<Object> getPath() {
        return path == null ? null : Collections.unmodifiableList(path);
    }

    /**
     * Get the error type as classified by the server. GitHub commonly uses values such as {@code FORBIDDEN},
     * {@code NOT_FOUND}, {@code RATE_LIMITED}, or {@code UNPROCESSABLE}. This is a GitHub extension to the GraphQL spec
     * and may be {@code null}.
     *
     * @return the error type, or {@code null}
     */
    @CheckForNull
    public String getType() {
        return type;
    }
}

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Code scanning alert instance for a repository
 *
 * <a href="https://docs.github.com/en/rest/reference/code-scanning"></a>
 */
public class GHCodeScanningAlertInstance {
    private String ref;
    private String analysis_key;
    private String environment;
    private GHCodeScanningAlertState state;
    private String commit_sha;
    private String[] classifications;
    private Message message;
    private Location location;

    /**
     * Ref that the alert instance was triggered on
     *
     * @return ref of the alert instance
     */
    public String getRef() {
        return ref;
    }

    /**
     * Analysis key of the alert instance
     *
     * @return the analysis key
     */
    public String getAnalysisKey() {
        return analysis_key;
    }

    /**
     * Environment the alert instance was triggered in
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * State of alert instance
     *
     * @return the state
     */
    public GHCodeScanningAlertState getState() {
        return state;
    }

    /**
     * Commit Sha that triggered the alert instance
     *
     * @return the commit sha
     */
    public String getCommitSha() {
        return commit_sha;
    }

    /**
     * Classifications of the alert instance
     *
     * @return the list of classifications
     */
    public List<String> getClassifications() {
        return Collections.unmodifiableList(Arrays.asList(classifications));
    }

    /**
     * Message object associated with the alert instance
     *
     * @return the message object
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Location of the alert instance (contains path, start_line, end_line, start_column, and end_column attributes)
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GHCodeScanningAlertInstance that = (GHCodeScanningAlertInstance) o;
        return Objects.equals(ref, that.ref) && Objects.equals(analysis_key, that.analysis_key)
                && Objects.equals(environment, that.environment) && state == that.state
                && Objects.equals(commit_sha, that.commit_sha) && Arrays.equals(classifications, that.classifications)
                && Objects.equals(message, that.message) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ref, analysis_key, environment, state, commit_sha, message, location);
        result = 31 * result + Arrays.hashCode(classifications);
        return result;
    }

    /**
     * Alert message
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class Message {
        private String text;

        /**
         * Alert message
         *
         * @return contents of the message
         */
        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Message message = (Message) o;
            return Objects.equals(text, message.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }
    }

    /**
     * Describe a region within a file for an alert.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class Location {
        private String path;
        private long start_line;
        private long end_line;
        private long start_column;
        private long end_column;

        /**
         * Path to the file containing the described code region
         *
         * @return path
         */
        public String getPath() {
            return path;
        }

        /**
         * Line number at the start of the code region.
         *
         * @return line number at the start of the code region
         */
        public long getStartLine() {
            return start_line;
        }

        /**
         * Line number at the end of the code region.
         *
         * @return line number at the end of the code region
         */
        public long getEndLine() {
            return end_line;
        }

        /**
         * Column number at the start of the code region.
         *
         * @return column number at the start of the code region
         */
        public long getStartColumn() {
            return start_column;
        }

        /**
         * Column number at the end of the code region.
         *
         * @return column number at the end of the code region
         */
        public long getEndColumn() {
            return end_column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Location location = (Location) o;
            return start_line == location.start_line && end_line == location.end_line
                    && start_column == location.start_column && end_column == location.end_column
                    && path.equals(location.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, start_line, end_line, start_column, end_column);
        }
    }
}

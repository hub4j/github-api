package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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

    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    static class Message {
        private String text;

        public String getText() {
            return text;
        }
    }

    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    static class Location {
        private String path;
        private long start_line;
        private long end_line;
        private long start_column;
        private long end_column;

        public String getPath() {
            return path;
        }

        public long getStartLine() {
            return start_line;
        }

        public long getEndLine() {
            return end_line;
        }

        public long getStartColumn() {
            return start_column;
        }

        public long getEndColumn() {
            return end_column;
        }
    }
}

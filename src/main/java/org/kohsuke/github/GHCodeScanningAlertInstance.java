package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GHCodeScanningAlertInstance {
    private String ref;
    private String analysis_key;
    private String environment;
    private GHCodeScanningAlertState state;
    private String commit_sha;
    private String[] classifications;
    private Message message;
    private Location location;

    public String getRef() {
        return ref;
    }

    public String getAnalysisKey() {
        return analysis_key;
    }

    public String getEnvironment() {
        return environment;
    }

    public GHCodeScanningAlertState getState() {
        return state;
    }

    public String getCommitSha() {
        return commit_sha;
    }

    public List<String> getClassifications() {
        return Collections.unmodifiableList(Arrays.asList(classifications));
    }

    public Message getMessage() {
        return message;
    }

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

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.util.Date;

/**
 * An object to track changes in projects_v2_item payloads.
 * <p>
 * Note that this is best effort only as nothing is documented in the GitHub documentation.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHProjectsV2ItemChanges {

    private FieldValue fieldValue;

    private FromToDate archivedAt;

    private FromTo previousProjectsV2ItemNodeId;

    public FieldValue getFieldValue() {
        return fieldValue;
    }

    public FromToDate getArchivedAt() {
        return archivedAt;
    }

    public FromTo getPreviousProjectsV2ItemNodeId() {
        return previousProjectsV2ItemNodeId;
    }

    public static class FieldValue {

        private String fieldNodeId;
        private String fieldType;

        public String getFieldNodeId() {
            return fieldNodeId;
        }

        public FieldType getFieldType() {
            return EnumUtils.getEnumOrDefault(FieldType.class, fieldType, FieldType.UNKNOWN);
        }
    }

    public static class FromTo {

        private String from;
        private String to;

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

    public static class FromToDate {

        private String from;
        private String to;

        public Date getFrom() {
            return GitHubClient.parseDate(from);
        }

        public Date getTo() {
            return GitHubClient.parseDate(to);
        }
    }

    public enum FieldType {

        TEXT, NUMBER, DATE, SINGLE_SELECT, ITERATION, UNKNOWN;
    }
}

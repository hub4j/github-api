package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.util.Date;

// TODO: Auto-generated Javadoc
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

    /**
     * Gets the field value.
     *
     * @return the field value
     */
    public FieldValue getFieldValue() {
        return fieldValue;
    }

    /**
     * Gets the archived at.
     *
     * @return the archived at
     */
    public FromToDate getArchivedAt() {
        return archivedAt;
    }

    /**
     * Gets the previous projects V 2 item node id.
     *
     * @return the previous projects V 2 item node id
     */
    public FromTo getPreviousProjectsV2ItemNodeId() {
        return previousProjectsV2ItemNodeId;
    }

    /**
     * The Class FieldValue.
     */
    public static class FieldValue {

        private String fieldNodeId;
        private String fieldType;

        /**
         * Gets the field node id.
         *
         * @return the field node id
         */
        public String getFieldNodeId() {
            return fieldNodeId;
        }

        /**
         * Gets the field type.
         *
         * @return the field type
         */
        public FieldType getFieldType() {
            return EnumUtils.getEnumOrDefault(FieldType.class, fieldType, FieldType.UNKNOWN);
        }
    }

    /**
     * The Class FromTo.
     */
    public static class FromTo {

        private String from;
        private String to;

        /**
         * Gets the from.
         *
         * @return the from
         */
        public String getFrom() {
            return from;
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        public String getTo() {
            return to;
        }
    }

    /**
     * The Class FromToDate.
     */
    public static class FromToDate {

        private String from;
        private String to;

        /**
         * Gets the from.
         *
         * @return the from
         */
        public Date getFrom() {
            return GitHubClient.parseDate(from);
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        public Date getTo() {
            return GitHubClient.parseDate(to);
        }
    }

    /**
     * The Enum FieldType.
     */
    public enum FieldType {

        /** The text. */
        TEXT, /** The number. */
 NUMBER, /** The date. */
 DATE, /** The single select. */
 SINGLE_SELECT, /** The iteration. */
 ITERATION, /** The unknown. */
 UNKNOWN;
    }
}

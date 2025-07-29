package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * An object to track changes in projects_v2_item payloads.
 * <p>
 * Note that this is best effort only as nothing is documented in the GitHub documentation.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHProjectsV2ItemChanges extends GitHubBridgeAdapterObject {

    /**
     * The Enum FieldType.
     */
    public enum FieldType {

        /** The date. */
        DATE,
        /** The iteration. */
        ITERATION,
        /** The number. */
        NUMBER,
        /** The single select. */
        SINGLE_SELECT,
        /** The text. */
        TEXT,
        /** The unknown. */
        UNKNOWN;
    }

    /**
     * The Class FieldValue.
     */
    public static class FieldValue {

        private String fieldNodeId;

        private String fieldType;
        /**
         * Create default FieldValue instance
         */
        public FieldValue() {
        }

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
         * Create default FromTo instance
         */
        public FromTo() {
        }

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
         * Create default FromToDate instance
         */
        public FromToDate() {
        }

        /**
         * Gets the from.
         *
         * @return the from
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getFrom() {
            return GitHubClient.parseInstant(from);
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getTo() {
            return GitHubClient.parseInstant(to);
        }
    }

    private FromToDate archivedAt;

    private FieldValue fieldValue;

    private FromTo previousProjectsV2ItemNodeId;

    /**
     * Create default GHProjectsV2ItemChanges instance
     */
    public GHProjectsV2ItemChanges() {
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
     * Gets the field value.
     *
     * @return the field value
     */
    public FieldValue getFieldValue() {
        return fieldValue;
    }

    /**
     * Gets the previous projects V 2 item node id.
     *
     * @return the previous projects V 2 item node id
     */
    public FromTo getPreviousProjectsV2ItemNodeId() {
        return previousProjectsV2ItemNodeId;
    }
}

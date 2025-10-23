package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Wrapper to define changed fields on label action="edited".
 *
 * @see GHEventPayload.Label
 */
@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public class GHLabelChanges {

    /**
     * Wrapper for changed values.
     */
    public static class GHFrom {

        private String from;

        /**
         * Create default GHFrom instance
         */
        public GHFrom() {
        }

        /**
         * Previous value that was changed.
         *
         * @return previous value
         */
        public String getFrom() {
            return from;
        }
    }

    private GHFrom color;
    private GHFrom name;

    /**
     * Create default GHLabelChanges instance
     */
    public GHLabelChanges() {
    }

    /**
     * Old label color.
     *
     * @return old label color (or null if not changed)
     */
    public GHFrom getColor() {
        return color;
    }

    /**
     * Old label name.
     *
     * @return old label name (or null if not changed)
     */
    public GHFrom getName() {
        return name;
    }
}

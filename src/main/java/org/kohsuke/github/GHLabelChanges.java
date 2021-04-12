package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wrapper to define changed fields on label action="edited"
 *
 * @see GHEventPayload.Label
 */
@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public class GHLabelChanges {

    private GHFrom name;
    private GHFrom color;

    /**
     * Old label name.
     *
     * @return old label name (or null if not changed)
     */
    public GHFrom getName() {
        return name;
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
     * Wrapper for changed values.
     */
    public static class GHFrom {
        private String from;

        /**
         * Previous value that was changed.
         *
         * @return previous value
         */
        public String getFrom() {
            return from;
        }
    }
}

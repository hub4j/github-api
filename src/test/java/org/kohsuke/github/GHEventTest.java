package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// TODO: Auto-generated Javadoc
/**
 * The Class GHEventTest.
 */
public class GHEventTest {

    /**
     * Function from GHEventInfo to transform string event to GHEvent which has been replaced by static mapping due to
     * complex parsing logic below
     */
    private static GHEvent oldTransformationFunction(String t) {
        if (t.endsWith("Event")) {
            t = t.substring(0, t.length() - 5);
        }
        for (GHEvent e : GHEvent.values()) {
            if (e.name().replace("_", "").equalsIgnoreCase(t)) {
                return e;
            }
        }
        return GHEvent.UNKNOWN;
    }

    /**
     * Regression test.
     */
    @Test
    public void regressionTest() {
        assertThat(GHEventInfo.transformTypeToGHEvent("NewlyAddedOrBogusEvent"), is(GHEvent.UNKNOWN));
        for (String eventInfoType : GHEventInfo.mapTypeStringToEvent.keySet()) {
            assertThat(GHEventInfo.transformTypeToGHEvent(eventInfoType), is(oldTransformationFunction(eventInfoType)));
        }
    }
}

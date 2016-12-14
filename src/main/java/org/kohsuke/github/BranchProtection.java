package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @see GHBranch#disableProtection()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
class BranchProtection {
    boolean enabled;
    RequiredStatusChecks requiredStatusChecks;

    static class RequiredStatusChecks {
        EnforcementLevel enforcement_level;
        List<String> contexts = new ArrayList<String>();
    }
}

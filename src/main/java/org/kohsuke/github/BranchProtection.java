package org.kohsuke.github;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @see GHBranch#disableProtection()
 */
class BranchProtection {
    boolean enabled;
    RequiredStatusChecks requiredStatusChecks;

    static class RequiredStatusChecks {
        EnforcementLevel enforcement_level;
        List<String> contexts = new ArrayList<String>();
    }
}

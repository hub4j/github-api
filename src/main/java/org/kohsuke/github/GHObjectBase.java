package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.GitHub;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHObjectBase {

    @JacksonInject
    final GitHub root;

    GHObjectBase() {
        this(GitHub.offline());
    }

    GHObjectBase(GitHub root) {
        this.root = root;
    }
}

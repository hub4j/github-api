package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a tag in {@link GHRepository}
 *
 * @see GHRepository#listTags()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHTag extends GHObjectBase {

    @JacksonInject(value = "org.kohsuke.github.GHRepository")
    private GHRepository owner;

    private String name;
    private GHCommit commit;

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return super.getRoot();
    }

    public String getName() {
        return name;
    }

    public GHCommit getCommit() {
        return commit;
    }
}

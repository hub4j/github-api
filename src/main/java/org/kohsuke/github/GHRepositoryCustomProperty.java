package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A custom property set on a repository in GitHub.
 *
 * @author gitPushPuppets
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHRepositoryCustomProperty {

    /**
     * Create default GHRepositoryCustomProperty instance
     */
    public GHRepositoryCustomProperty() {
    }

    private String property_name;
    private String value;

    /**
     * Gets property_name
     *
     * @return the property_name
     */
    public String getPropertyName() {
        return property_name;
    }

    /**
     * Gets property value
     *
     * @return the property value
     */
    public String getValue() {
        return value;
    }

}

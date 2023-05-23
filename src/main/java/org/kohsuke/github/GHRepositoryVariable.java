package org.kohsuke.github;

/**
 * The type Gh repository variable.
 *
 * @author garridobarrera
 */
public class GHRepositoryVariable {
    private String name;
    private String value;
    private String createdAt;
    private String updatedAt;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}

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
     * Sets name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }
}

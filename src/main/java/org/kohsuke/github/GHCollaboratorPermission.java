package org.kohsuke.github;

/**
 * @author Christoph Rieser
 */
public enum GHCollaboratorPermission {
    PULL("{ \"permission\": \"pull\" }"), PUSH("{ \"permission\": \"push\" }"), ADMIN("{ \"permission\": \"admin\" }");

    final private String str;

    private GHCollaboratorPermission(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}

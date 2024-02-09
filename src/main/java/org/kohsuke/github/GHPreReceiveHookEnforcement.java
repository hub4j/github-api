package org.kohsuke.github;

public enum GHPreReceiveHookEnforcement {
    DISABLED, ENABLED, TESTING;

    public String toParameterValue() {
        return this.name().toLowerCase();
    }
}

package org.kohsuke.github;

import java.util.Locale;

public abstract class GHPreReceiveHook extends GHObject {
    long id;

    String name;

    String enforcement;

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEnforcement() {
        return enforcement;
    }

    public GHPreReceiveHookEnforcement getEnforcementType() {
        return Enum.valueOf(GHPreReceiveHookEnforcement.class, this.enforcement.toUpperCase(Locale.ENGLISH));
    }
    public GHPreReceiveHook() {
    }

}

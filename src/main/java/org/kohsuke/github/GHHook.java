package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class GHHook extends GHObject {
    String name;
    List<String> events;
    boolean active;
    Map<String,String> config;

    public String getName() {
        return name;
    }

    public EnumSet<GHEvent> getEvents() {
        EnumSet<GHEvent> s = EnumSet.noneOf(GHEvent.class);
        for (String e : events)
            s.add(Enum.valueOf(GHEvent.class,e.toUpperCase(Locale.ENGLISH)));
        return s;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    /**
     * Deletes this hook.
     */
    public void delete() throws IOException {
        new Requester(getRoot()).method("DELETE").to(getApiRoute());
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    abstract GitHub getRoot();

    abstract String getApiRoute();
}

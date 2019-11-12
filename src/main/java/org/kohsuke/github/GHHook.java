package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
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
        for (String e : events) {
            if (e.equals("*"))  s.add(GHEvent.ALL);
            else                s.add(Enum.valueOf(GHEvent.class, e.toUpperCase(Locale.ENGLISH)));
        }
        return s;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    /**
     * @see <a href="https://developer.github.com/v3/repos/hooks/#ping-a-hook">Ping hook</a>
     */
    public void ping() throws IOException {
        getRoot().createRequester().method("POST").to(getApiRoute() + "/pings");
    }

    /**
     * Deletes this hook.
     */
    public void delete() throws IOException {
        getRoot().createRequester().method("DELETE").to(getApiRoute());
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

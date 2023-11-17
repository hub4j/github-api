package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The type GHHook.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public abstract class GHHook extends GHObject {

    /** The name. */
    String name;

    /** The events. */
    List<String> events;

    /** The active. */
    boolean active;

    /** The config. */
    Map<String, String> config;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    public EnumSet<GHEvent> getEvents() {
        EnumSet<GHEvent> s = EnumSet.noneOf(GHEvent.class);
        for (String e : events) {
            s.add(e.equals("*") ? GHEvent.ALL : EnumUtils.getEnumOrDefault(GHEvent.class, e, GHEvent.UNKNOWN));
        }
        return s;
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets config.
     *
     * @return the config
     */
    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    /**
     * Ping.
     *
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/repos/hooks/#ping-a-hook">Ping hook</a>
     */
    public void ping() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiRoute() + "/pings").send();
    }

    /**
     * Deletes this hook.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Root.
     *
     * @return the git hub
     */
    abstract GitHub root();

    /**
     * Gets the api route.
     *
     * @return the api route
     */
    abstract String getApiRoute();
}

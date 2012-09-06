package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public final class GHHook {
    /**
     * Repository that the hook belongs to.
     */
    /*package*/ transient GHRepository repository;
    
    String created_at, updated_at, name;
    List<String> events;
    boolean active;
    Map<String,String> config;
    int id;
    
    /*package*/ GHHook wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    public String getName() {
        return name;
    }

    public EnumSet<GHEvent> getEvents() {
        EnumSet<GHEvent> s = EnumSet.noneOf(GHEvent.class);
        for (String e : events)
            Enum.valueOf(GHEvent.class,e.toUpperCase(Locale.ENGLISH));
        return s;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    public int getId() {
        return id;
    }

    /**
     * Deletes this hook.
     */
    public void delete() throws IOException {
        new Poster(repository.root).withCredential().method("DELETE").to(String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), id));
    }
}

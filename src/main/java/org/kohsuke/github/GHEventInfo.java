package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents an event.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHEventInfo {
    private GitHub root;

    // we don't want to expose Jackson dependency to the user. This needs databinding
    private ObjectNode payload;

    private String created_at;
    private String type;

    // these are all shallow objects
    private GHEventRepository repo;
    private GHUser actor;
    private GHOrganization org;

    /**
     * Inside the event JSON model, GitHub uses a slightly different format.
     */
    public static class GHEventRepository {
        private int id;
        private String url;     // repository API URL
        private String name;    // owner/repo
    }

    public GHEvent getType() {
        String t = type;
        if (t.endsWith("Event"))    t=t.substring(0,t.length()-5);
        for (GHEvent e : GHEvent.values()) {
            if (e.name().replace("_","").equalsIgnoreCase(t))
                return e;
        }
        return null;    // unknown event type
    }

    /*package*/ GHEventInfo wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * Repository where the change was made.
     */
    public GHRepository getRepository() throws IOException {
        return root.getRepository(repo.name);
    }
    
    public GHUser getActor() throws IOException {
        return root.getUser(actor.getLogin());
    }

    public GHOrganization getOrganization() throws IOException {
        return (org==null || org.getLogin()==null) ? null : root.getOrganization(org.getLogin());
    }

    /**
     * Retrieves the payload.
     * 
     * @param type
     *      Specify one of the {@link GHEventPayload} subtype that defines a type-safe access to the payload.
     *      This must match the {@linkplain #getType() event type}.
     */
    public <T extends GHEventPayload> T getPayload(Class<T> type) throws IOException {
        T v = GitHub.MAPPER.readValue(payload.traverse(), type);
        v.wrapUp(root);
        return v;
    }
}

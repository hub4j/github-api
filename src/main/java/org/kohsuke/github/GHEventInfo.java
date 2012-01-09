package org.kohsuke.github;

import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Date;

/**
 * Represents an event.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHEventInfo {
    private GitHub root;

    private ObjectNode payload;

    private String created_at;
    private String type;

    // these are all shallow objects
    private GHRepository repo;
    private GHUser actor;
    private GHOrganization org;
    
    public GHEvent getType() {
        for (GHEvent e : GHEvent.values()) {
            if (e.name().replace("_","").equalsIgnoreCase(type))
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
        return root.getRepository(repo.getName());
    }
    
    public GHUser getActor() throws IOException {
        return root.getUser(actor.getLogin());
    }

    public GHOrganization getOrganization() throws IOException {
        return (org==null || org.getLogin()==null) ? null : root.getOrganization(org.getLogin());
    }
}

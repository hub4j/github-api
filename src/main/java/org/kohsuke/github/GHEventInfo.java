package org.kohsuke.github;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Date;

/**
 * Represents an event.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHEventInfo {
    private GitHub root;

    // we don't want to expose Jackson dependency to the user. This needs databinding
    private ObjectNode payload;

    private long id;
    private String created_at;
    private String type;

    // these are all shallow objects
    private GHEventRepository repo;
    private GHUser actor;
    private GHOrganization org;

    /**
     * Inside the event JSON model, GitHub uses a slightly different format.
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
                    "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "JSON API")
    public static class GHEventRepository {
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        private long id;
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        private String url; // repository API URL
        private String name; // owner/repo
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public GHEvent getType() {
        String t = type;
        if (t.endsWith("Event"))
            t = t.substring(0, t.length() - 5);
        for (GHEvent e : GHEvent.values()) {
            if (e.name().replace("_", "").equalsIgnoreCase(t))
                return e;
        }
        return null; // unknown event type
    }

    GHEventInfo wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return GitHubClient.parseDate(created_at);
    }

    /**
     * Gets repository.
     *
     * @return Repository where the change was made.
     * @throws IOException
     *             on error
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "The field comes from JSON deserialization")
    public GHRepository getRepository() throws IOException {
        return root.getRepository(repo.name);
    }

    /**
     * Gets actor.
     *
     * @return the {@link GHUser} actor for this event.
     * @throws IOException
     *             on error
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "The field comes from JSON deserialization")
    public GHUser getActor() throws IOException {
        return root.getUser(actor.getLogin());
    }

    /**
     * Gets actor login.
     *
     * @return the login of the actor.
     * @throws IOException
     *             on error
     */
    public String getActorLogin() throws IOException {
        return actor.getLogin();
    }

    /**
     * Gets organization.
     *
     * @return the organization
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
            justification = "The field comes from JSON deserialization")
    public GHOrganization getOrganization() throws IOException {
        return (org == null || org.getLogin() == null) ? null : root.getOrganization(org.getLogin());
    }

    /**
     * Retrieves the payload.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            Specify one of the {@link GHEventPayload} subtype that defines a type-safe access to the payload. This
     *            must match the {@linkplain #getType() event type}.
     * @return parsed event payload
     * @throws IOException
     *             if payload cannot be parsed
     */
    public <T extends GHEventPayload> T getPayload(Class<T> type) throws IOException {
        T v = GitHubClient.getMappingObjectReader(root).readValue(payload.traverse(), type);
        v.wrapUp(root);
        return v;
    }
}

package org.kohsuke.github;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * Represents an event.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHEventInfo extends GitHubInteractiveObject {
    // we don't want to expose Jackson dependency to the user. This needs databinding
    private ObjectNode payload;

    private long id;
    private String created_at;

    /**
     * Representation of GitHub Event API Event Type.
     *
     * This is not the same as the values used for hook methods such as
     * {@link GHRepository#createHook(String, Map, Collection, boolean)}.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/github-event-types">GitHub event
     *      types</a>
     */
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

    /** The Constant mapTypeStringToEvent. */
    static final Map<String, GHEvent> mapTypeStringToEvent = createEventMap();

    /**
     * Map for GitHub Event API Event Type to GHEvent.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/github-event-types">GitHub event
     *      types</a>
     */
    private static Map<String, GHEvent> createEventMap() {
        HashMap<String, GHEvent> map = new HashMap<>();
        map.put("CommitCommentEvent", GHEvent.COMMIT_COMMENT);
        map.put("CreateEvent", GHEvent.CREATE);
        map.put("DeleteEvent", GHEvent.DELETE);
        map.put("ForkEvent", GHEvent.FORK);
        map.put("GollumEvent", GHEvent.GOLLUM);
        map.put("IssueCommentEvent", GHEvent.ISSUE_COMMENT);
        map.put("IssuesEvent", GHEvent.ISSUES);
        map.put("MemberEvent", GHEvent.MEMBER);
        map.put("PublicEvent", GHEvent.PUBLIC);
        map.put("PullRequestEvent", GHEvent.PULL_REQUEST);
        map.put("PullRequestReviewEvent", GHEvent.PULL_REQUEST_REVIEW);
        map.put("PullRequestReviewCommentEvent", GHEvent.PULL_REQUEST_REVIEW_COMMENT);
        map.put("PushEvent", GHEvent.PUSH);
        map.put("ReleaseEvent", GHEvent.RELEASE);
        map.put("WatchEvent", GHEvent.WATCH);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Transform type to GH event.
     *
     * @param type the type
     * @return the GH event
     */
    static GHEvent transformTypeToGHEvent(String type) {
        return mapTypeStringToEvent.getOrDefault(type, GHEvent.UNKNOWN);
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public GHEvent getType() {
        return transformTypeToGHEvent(type);
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
        return root().getRepository(repo.name);
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
        return root().getUser(actor.getLogin());
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
        return (org == null || org.getLogin() == null) ? null : root().getOrganization(org.getLogin());
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
        T v = GitHubClient.getMappingObjectReader(root()).readValue(payload.traverse(), type);
        v.lateBind();
        return v;
    }
}

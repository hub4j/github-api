package org.kohsuke.github;

import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * A Projects V2 item in the organization.
 * <p>
 * Projects V2 are not attached to a repository but to an organization, even if it is possible to create shortcuts at
 * the repository level.
 * <p>
 * This event exposes the GraphQL object (more or less - the ids are handled differently for instance) directly. The new
 * Projects V2 API is only available through GraphQL so for now you cannot execute any actions on this object.
 *
 * @author Guillaume Smet
 * @see <a href=
 *      "https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects">The
 *      GraphQL API for Projects V2</a>
 */
public class GHProjectsV2Item extends GHObject {

    private String projectNodeId;
    private String contentNodeId;
    private String contentType;

    private GHUser creator;
    private String archivedAt;

    public String getProjectNodeId() {
        return projectNodeId;
    }

    public String getContentNodeId() {
        return contentNodeId;
    }

    public ContentType getContentType() {
        return EnumUtils.getEnumOrDefault(ContentType.class, contentType, ContentType.UNKNOWN);
    }

    public GHUser getCreator() throws IOException {
        return root().intern(creator);
    }

    public Date getArchivedAt() {
        return GitHubClient.parseDate(archivedAt);
    }

    public URL getHtmlUrl() {
        throw new IllegalStateException(getClass().getName() + " does not offer a HTML URL.");
    }

    public enum ContentType {
        ISSUE, DRAFTISSUE, PULLREQUEST, UNKNOWN;
    }
}

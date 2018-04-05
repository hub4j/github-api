/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
        "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" }, justification = "JSON API")
    public static class GHEventRepository {
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        private int id;
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
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

    public long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * Repository where the change was made.
     */
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" }, 
            justification = "The field comes from JSON deserialization")
    public GHRepository getRepository() throws IOException {
        return root.getRepository(repo.name);
    }
    
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" }, 
            justification = "The field comes from JSON deserialization")
    public GHUser getActor() throws IOException {
        return root.getUser(actor.getLogin());
    }

    /**
     * Quick way to just get the actor of the login.
     */
    public String getActorLogin() throws IOException {
        return actor.getLogin();
    }

    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" }, 
            justification = "The field comes from JSON deserialization")
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

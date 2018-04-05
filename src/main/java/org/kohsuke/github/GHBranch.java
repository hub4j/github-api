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

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import static org.kohsuke.github.Previews.*;

/**
 * A branch in a repository.
 *
 * @author Yusuke Kokubo
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
    "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
public class GHBranch {
    private GitHub root;
    private GHRepository owner;

    private String name;
    private Commit commit;
    @JsonProperty("protected")
    private boolean protection;
    private String protection_url;


    public static class Commit {
        String sha;

        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url;
    }

    public GitHub getRoot() {
        return root;
    }

    /**
     * Repository that this branch is in.
     */
    public GHRepository getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns true if the push to this branch is restricted via branch protection.
     */
    @Preview @Deprecated
    public boolean isProtected() {
        return protection;
    }

    /**
     * Returns API URL that deals with the protection of this branch.
     */
    @Preview @Deprecated
    public URL getProtectionUrl() {
        return GitHub.parseURL(protection_url);
    }

    @Preview @Deprecated
    public GHBranchProtection getProtection() throws IOException {
        return root.retrieve().withPreview(LOKI).to(protection_url, GHBranchProtection.class);
    }

    /**
     * The commit that this branch currently points to.
     */
    public String getSHA1() {
        return commit.sha;
    }

    /**
     * Disables branch protection and allows anyone with push access to push changes.
     */
    @Preview @Deprecated
    public void disableProtection() throws IOException {
        new Requester(root).method("DELETE").withPreview(LOKI).to(protection_url);
    }

    /**
     * Enables branch protection to control what commit statuses are required to push.
     *
     * @see GHCommitStatus#getContext()
     */
    @Preview @Deprecated
    public GHBranchProtectionBuilder enableProtection() {
        return new GHBranchProtectionBuilder(this);
    }

    // backward compatibility with previous signature
    @Deprecated
    public void enableProtection(EnforcementLevel level, Collection<String> contexts) throws IOException {
        switch (level) {
        case OFF:
            disableProtection();
            break;
        case NON_ADMINS:
        case EVERYONE:
            enableProtection()
                .addRequiredChecks(contexts)
                .includeAdmins(level==EnforcementLevel.EVERYONE)
                .enable();
            break;
        }
    }

    String getApiRoute() {
        return owner.getApiTailUrl("/branches/"+name);
    }

    @Override
    public String toString() {
        final String url = owner != null ? owner.getUrl().toString() : "unknown";
        return "Branch:" + name + " in " + url;
    }

    /*package*/ GHBranch wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}

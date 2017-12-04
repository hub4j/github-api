/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import java.io.IOException;
import java.util.Date;

/**
 * Represents your subscribing to a repository / conversation thread..
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getSubscription()
 * @see GHThread#getSubscription()
 */
public class GHSubscription {
    private String created_at, url, repository_url, reason;
    private boolean subscribed, ignored;

    private GitHub root;
    private GHRepository repo;

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public String getUrl() {
        return url;
    }

    public String getRepositoryUrl() {
        return repository_url;
    }

    public String getReason() {
        return reason;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public GHRepository getRepository() {
        return repo;
    }

    /**
     * Removes this subscription.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(url);
    }

    GHSubscription wrapUp(GHRepository repo) {
        this.repo = repo;
        return wrapUp(repo.root);
    }

    GHSubscription wrapUp(GitHub root) {
        this.root = root;
        return this;
    }
}

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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * SSH public key.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON API")
public class GHKey {
    /*package almost final*/ GitHub root;

    protected String url, key, title;
    protected boolean verified;
    protected int id;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Something like "https://api.github.com/user/keys/73593"
     */
    public String getUrl() {
        return url;
    }

    public GitHub getRoot() {
        return root;
    }
    
    public boolean isVerified() {
        return verified;
    }

    /*package*/ GHKey wrap(GitHub root) {
        this.root = root;
        return this;
    }

    public String toString() {
        return new ToStringBuilder(this).append("title",title).append("id",id).append("key",key).toString();
    }
}

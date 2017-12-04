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
import java.net.URL;

/**
 * Asset in a release.
 *
 * @see GHRelease#getAssets()
 */
public class GHAsset extends GHObject {
    GitHub root;
    GHRepository owner;
    private String name;
    private String label;
    private String state;
    private String content_type;
    private long size;
    private long download_count;
    private String browser_download_url;

    public String getContentType() {
        return content_type;
    }

    public void setContentType(String contentType) throws IOException {
        edit("content_type", contentType);
        this.content_type = contentType;
    }

    public long getDownloadCount() {
        return download_count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) throws IOException {
        edit("label", label);
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public long getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public String getBrowserDownloadUrl() {
        return browser_download_url;
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getApiRoute());
    }

    public void delete() throws IOException {
        new Requester(root).method("DELETE").to(getApiRoute());
    }


    private String getApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/releases/assets/" + id;
    }

    GHAsset wrap(GHRelease release) {
        this.owner = release.getOwner();
        this.root = owner.root;
        return this;
    }

    public static GHAsset[] wrap(GHAsset[] assets, GHRelease release) {
        for (GHAsset aTo : assets) {
            aTo.wrap(release);
        }
        return assets;
    }
}

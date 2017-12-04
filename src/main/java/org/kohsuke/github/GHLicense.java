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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.github.Previews.*;

/**
 * The GitHub Preview API's license information
 * <p>
 * WARNING: This uses a PREVIEW API - subject to change.
 *
 * @author Duncan Dickinson
 * @see GitHub#getLicense(String)
 * @see GHRepository#getLicense()
 * @see <a href="https://developer.github.com/v3/licenses/">https://developer.github.com/v3/licenses/</a>
 */
@Preview @Deprecated
@SuppressWarnings({"UnusedDeclaration"})
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
        "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHLicense extends GHObject {
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC") // root is set before the object is returned to the app
    /*package almost final*/ GitHub root;

    // these fields are always present, even in the short form
    protected String key, name;

    // the rest is only after populated
    protected Boolean featured;

    protected String html_url, description, category, implementation, body;

    protected List<String> required = new ArrayList<String>();
    protected List<String> permitted = new ArrayList<String>();
    protected List<String> forbidden = new ArrayList<String>();

    /**
     * @return a mnemonic for the license
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the license name
     */
    public String getName() {
        return name;
    }

    /**
     * @return API URL of this object.
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "urlToString")
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * Featured licenses are bold in the new repository drop-down
     *
     * @return True if the license is featured, false otherwise
     */
    public Boolean isFeatured() throws IOException {
        populate();
        return featured;
    }

    public URL getHtmlUrl() throws IOException {
        populate();
        return GitHub.parseURL(html_url);
    }

    public String getDescription() throws IOException {
        populate();
        return description;
    }

    public String getCategory() throws IOException {
        populate();
        return category;
    }

    public String getImplementation() throws IOException {
        populate();
        return implementation;
    }

    public List<String> getRequired() throws IOException {
        populate();
        return required;
    }

    public List<String> getPermitted() throws IOException {
        populate();
        return permitted;
    }

    public List<String> getForbidden() throws IOException {
        populate();
        return forbidden;
    }

    public String getBody() throws IOException {
        populate();
        return body;
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    protected synchronized void populate() throws IOException {
        if (description!=null)    return; // already populated

        root.retrieve().withPreview(DRAX).to(url, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GHLicense)) return false;

        GHLicense that = (GHLicense) o;
        return this.url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /*package*/ GHLicense wrap(GitHub root) {
        this.root = root;
        return this;
    }
}

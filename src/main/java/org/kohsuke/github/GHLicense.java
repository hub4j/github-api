/*
 * The MIT License
 *
 * Copyright (c) 2016, Duncan Dickinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * The GitHub Preview API's license information.
 *
 * @author Duncan Dickinson
 * @see GitHub#getLicense(String) GitHub#getLicense(String)
 * @see GHRepository#getLicense() GHRepository#getLicense()
 * @see <a href="https://developer.github.com/v3/licenses/">https://developer.github.com/v3/licenses/</a>
 */
@SuppressWarnings({ "UnusedDeclaration" })
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHLicense extends GHObject {

    /** The featured. */
    // the rest is only after populated
    protected Boolean featured;

    /** The forbidden. */
    protected List<String> forbidden = new ArrayList<String>();

    /** The body. */
    protected String htmlUrl, description, category, implementation, body;

    /** The name. */
    // these fields are always present, even in the short form
    protected String key, name, spdxId;

    /** The permitted. */
    protected List<String> permitted = new ArrayList<String>();

    /** The required. */
    protected List<String> required = new ArrayList<String>();

    /**
     * Create default GHLicense instance
     */
    public GHLicense() {
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GHLicense))
            return false;

        GHLicense that = (GHLicense) o;
        return Objects.equals(getUrl(), that.getUrl());
    }

    /**
     * Gets body.
     *
     * @return the body
     * @throws IOException
     *             the io exception
     */
    public String getBody() throws IOException {
        populate();
        return body;
    }

    /**
     * Gets category.
     *
     * @return the category
     * @throws IOException
     *             the io exception
     */
    public String getCategory() throws IOException {
        populate();
        return category;
    }

    /**
     * Gets description.
     *
     * @return the description
     * @throws IOException
     *             the io exception
     */
    public String getDescription() throws IOException {
        populate();
        return description;
    }

    /**
     * Gets forbidden.
     *
     * @return the forbidden
     * @throws IOException
     *             the io exception
     */
    public List<String> getForbidden() throws IOException {
        populate();
        return Collections.unmodifiableList(forbidden);
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public URL getHtmlUrl() throws IOException {
        populate();
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets implementation.
     *
     * @return the implementation
     * @throws IOException
     *             the io exception
     */
    public String getImplementation() throws IOException {
        populate();
        return implementation;
    }

    /**
     * Gets key.
     *
     * @return a mnemonic for the license
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets name.
     *
     * @return the license name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets permitted.
     *
     * @return the permitted
     * @throws IOException
     *             the io exception
     */
    public List<String> getPermitted() throws IOException {
        populate();
        return Collections.unmodifiableList(permitted);
    }

    /**
     * Gets required.
     *
     * @return the required
     * @throws IOException
     *             the io exception
     */
    public List<String> getRequired() throws IOException {
        populate();
        return Collections.unmodifiableList(required);
    }

    /**
     * Gets SPDX ID.
     *
     * @return the spdx id
     */
    public String getSpdxId() {
        return spdxId;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getUrl());
    }

    /**
     * Featured licenses are bold in the new repository drop-down.
     *
     * @return True if the license is featured, false otherwise
     * @throws IOException
     *             the io exception
     */
    public Boolean isFeatured() throws IOException {
        populate();
        return featured;
    }

    /**
     * Fully populate the data by retrieving missing data.
     * <p>
     * Depending on the original API call where this object is created, it may not contain everything.
     *
     * @throws IOException
     *             the io exception
     */
    protected synchronized void populate() throws IOException {
        if (description != null)
            return; // already populated

        if (isOffline()) {
            return; // cannot populate, will have to live with what we have
        }

        URL url = getUrl();
        if (url != null) {
            root().createRequest().setRawUrlPath(url.toString()).fetchInto(this);
        }
    }
}

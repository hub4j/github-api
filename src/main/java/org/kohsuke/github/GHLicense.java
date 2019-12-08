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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The GitHub Preview API's license information
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
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    // root is set before the object is returned to the app
    /* package almost final */ GitHub root;

    // these fields are always present, even in the short form
    protected String key, name;

    // the rest is only after populated
    protected Boolean featured;

    protected String html_url, description, category, implementation, body;

    protected List<String> required = new ArrayList<String>();
    protected List<String> permitted = new ArrayList<String>();
    protected List<String> forbidden = new ArrayList<String>();

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
     * @throws IOException
     *             the io exception
     */
    public Boolean isFeatured() throws IOException {
        populate();
        return featured;
    }

    public URL getHtmlUrl() throws IOException {
        populate();
        return GitHub.parseURL(html_url);
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
     * Gets required.
     *
     * @return the required
     * @throws IOException
     *             the io exception
     */
    public List<String> getRequired() throws IOException {
        populate();
        return required;
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
        return permitted;
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
        return forbidden;
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

        root.retrieve().to(url, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GHLicense))
            return false;

        GHLicense that = (GHLicense) o;
        return this.url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    GHLicense wrap(GitHub root) {
        this.root = root;
        return this;
    }
}

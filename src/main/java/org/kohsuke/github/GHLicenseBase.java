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

import java.net.URL;

/**
 * The basic information for GitHub API licenses - as use in a number of
 * API calls that only return the basic details
 * <p>
 * WARNING: This uses a PREVIEW API - you must use {@link org.kohsuke.github.extras.PreviewHttpConnector}
 *
 * @author Duncan Dickinson
 * @see <a href="https://developer.github.com/v3/licenses/">https://developer.github.com/v3/licenses/</a>
 * @see GitHub#listLicenses()
 * @see GHRepository#getLicense()
 * @see GHLicense GHLicense subclass for the more comprehensive listing of properties
 */
public class GHLicenseBase {

    protected String key, name, url;
    protected Boolean featured;

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
    public Boolean isFeatured() {
        return featured;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GHLicenseBase)) return false;

        GHLicenseBase that = (GHLicenseBase) o;

        return getUrl().equals(that.getUrl());

    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

    @Override
    public String toString() {
        return "GHLicenseBase{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", featured=" + featured +
                '}';
    }
}

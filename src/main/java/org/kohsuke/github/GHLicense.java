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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The GitHub Preview API's license information
 * <p>
 * WARNING: This uses a PREVIEW API - you must use {@link org.kohsuke.github.extras.PreviewHttpConnector}
 *
 * @author Duncan Dickinson
 * @see GitHub#getLicense(String)
 * @see GHRepository#getFullLicense()
 * @see <a href="https://developer.github.com/v3/licenses/">https://developer.github.com/v3/licenses/</a>
 */
public class GHLicense extends GHLicenseBase {

    protected String html_url, description, category, implementation, body;

    protected List<String> required = new ArrayList<String>();
    protected List<String> permitted = new ArrayList<String>();
    protected List<String> forbidden = new ArrayList<String>();

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImplementation() {
        return implementation;
    }

    public List<String> getRequired() {
        return required;
    }

    public List<String> getPermitted() {
        return permitted;
    }

    public List<String> getForbidden() {
        return forbidden;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "GHLicense{" +
                "html_url='" + html_url + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", implementation='" + implementation + '\'' +
                ", body='" + body + '\'' +
                ", required=" + required +
                ", permitted=" + permitted +
                ", forbidden=" + forbidden +
                ", htmlUrl=" + getHtmlUrl() +
                "} " + super.toString();
    }
}

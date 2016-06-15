/*
 *    Copyright $year slavinson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.net.URL;

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
     * API URL of this object.
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "urlToString")
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * @return
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

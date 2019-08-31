/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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
 *
 */

package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockRuleFactory {
    private String urlToMock = System.getProperty("wiremock.record");

    public WireMockRule getRule(int port) {
        return getRule(wireMockConfig().port(port));
    }

    public WireMockRule getRule(Options options) {
        if (urlToMock != null && !urlToMock.isEmpty()) {
            return new WireMockRecorderRule(options, urlToMock);
        } else {
            return new WireMockRule(options);
        }
    }


    private class WireMockRecorderRule extends WireMockRule {
        //needed for WireMockRule file location
        private String mappingLocation = "src/test/resources";

        public WireMockRecorderRule(Options options, String url) {
            super(options);
            this.stubFor(get(urlMatching(".*")).atPriority(10).willReturn(aResponse().proxiedFrom(url)));
            this.enableRecordMappings(new SingleRootFileSource(mappingLocation + "/mappings"),
                    new SingleRootFileSource(mappingLocation + "/__files"));
        }
    }
}

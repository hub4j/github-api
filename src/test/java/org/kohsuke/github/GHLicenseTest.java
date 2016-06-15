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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.extras.PreviewHttpConnector;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class GHLicenseTest extends Assert {
    private GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        gitHub = new GitHubBuilder()
                .fromCredentials()
                .withConnector(new PreviewHttpConnector())
                .build();
    }

    @Test
    public void listLicenses() throws IOException {
        List<GHLicenseBase> licenses = gitHub.listLicenses();
        assertTrue(licenses.size() > 0);
    }

    @Test
    public void listLicensesCheckIndividualLicense() throws IOException {
        List<GHLicenseBase> licenses = gitHub.listLicenses();
        for (GHLicenseBase lic: licenses) {
            if (lic.getKey().equals("mit")) {
                assertTrue(lic.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
                return;
            }
        }
        fail("The MIT license was not found");
    }

    @Test
    public void getLicense() throws IOException {
        String key = "mit";
        GHLicense license = gitHub.getLicense(key);
        assertNotNull(license);
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The HTML URL is correct", license.getHtmlUrl().equals(new URL("http://choosealicense.com/licenses/mit/")));
    }

    @Test(expected = IOException.class)
    public void ListLicensesWithoutPreviewConnection() throws IOException {
        GitHub.connect().listLicenses();
    }

    @Test
    public void checkRepositoryLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("kohsuke/github-api");
        GHLicenseBase license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct", license.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
    }

    @Test
    public void checkRepositoryFullLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("kohsuke/github-api");
        GHLicense license = repo.getFullLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct", license.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
        assertTrue("The HTML URL is correct", license.getHtmlUrl().equals(new URL("http://choosealicense.com/licenses/mit/")));
    }
}

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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.extras.PreviewHttpConnector;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author Duncan Dickinson
 */
public class GHLicenseTest extends Assert {
    private GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        gitHub = new GitHubBuilder()
                .fromCredentials()
                .withConnector(new PreviewHttpConnector())
                .build();
    }

    /**
     * Basic test to ensure that the list of licenses from {@link GitHub#listLicenses()} is returned
     *
     * @throws IOException
     */
    @Test
    public void listLicenses() throws IOException {
        List<GHLicenseBase> licenses = gitHub.listLicenses();
        assertTrue(licenses.size() > 0);
    }

    /**
     * Tests that {@link GitHub#listLicenses()} returns the MIT license
     * in the expected manner.
     *
     * @throws IOException
     */
    @Test
    public void listLicensesCheckIndividualLicense() throws IOException {
        List<GHLicenseBase> licenses = gitHub.listLicenses();
        for (GHLicenseBase lic : licenses) {
            if (lic.getKey().equals("mit")) {
                assertTrue(lic.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
                return;
            }
        }
        fail("The MIT license was not found");
    }

    /**
     * Checks that the request for an individual license using {@link GitHub#getLicense(String)}
     * returns expected values (not all properties are checked)
     *
     * @throws IOException
     */
    @Test
    public void getLicense() throws IOException {
        String key = "mit";
        GHLicense license = gitHub.getLicense(key);
        assertNotNull(license);
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The HTML URL is correct", license.getHtmlUrl().equals(new URL("http://choosealicense.com/licenses/mit/")));
    }

    /**
     * Attempts to list the licenses with a non-preview connection
     *
     * @throws IOException is expected to be thrown
     */
    @Test(expected = IOException.class)
    public void ListLicensesWithoutPreviewConnection() throws IOException {
        GitHub.connect().listLicenses();
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)}
     * and checks that the license is correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("kohsuke/github-api");
        GHLicenseBase license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct", license.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
    }

    /**
     * Accesses the 'atom/atom' repo using {@link GitHub#getRepository(String)}
     * and checks that the license is correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicenseAtom() throws IOException {
        GHRepository repo = gitHub.getRepository("atom/atom");
        GHLicenseBase license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct", license.getUrl().equals(new URL("https://api.github.com/licenses/mit")));
    }

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)}
     * and checks that the license is correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicensePomes() throws IOException {
        GHRepository repo = gitHub.getRepository("pomes/pomes");
        GHLicenseBase license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("apache-2.0"));
        assertTrue("The name is correct", license.getName().equals("Apache License 2.0"));
        assertTrue("The URL is correct", license.getUrl().equals(new URL("https://api.github.com/licenses/apache-2.0")));
    }

    /**
     * Accesses the 'dedickinson/test-repo' repo using {@link GitHub#getRepository(String)}
     * and checks that *no* license is returned as the repo doesn't have one
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryWithoutLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("dedickinson/test-repo");
        GHLicenseBase license = repo.getLicense();
        assertNull("There is no license", license);
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)}
     * and then calls {@link GHRepository#getFullLicense()} and checks that certain
     * properties are correct
     *
     * @throws IOException
     */
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

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)}
     * and then calls {@link GHRepository#getLicenseContent()} and checks that certain
     * properties are correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicenseContent() throws IOException {
        GHRepository repo = gitHub.getRepository("pomes/pomes");
        GHContent content = repo.getLicenseContent();
        assertNotNull("The license content is populated", content);
        assertTrue("The type is 'file'", content.getType().equals("file"));
        assertTrue("The license file is 'LICENSE'", content.getName().equals("LICENSE"));

        if (content.getEncoding().equals("base64")) {
            String licenseText = new String(IOUtils.toByteArray(content.read()));
            assertTrue("The license appears to be an Apache License", licenseText.contains("Apache License"));
        } else {
            fail("Expected the license to be Base64 encoded but instead it was " + content.getEncoding());
        }
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)}
     * but without using {@link PreviewHttpConnector} and ensures that the {@link GHRepository#getLicense()}
     * call just returns null rather than raising an exception. This should indicate that
     * non-preview connection requests aren't affected by the change in functionality
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicenseWithoutPreviewConnection() throws IOException {
        GHRepository repo = GitHub.connect().getRepository("kohsuke/github-api");
        assertNull(repo.getLicense());
    }
}

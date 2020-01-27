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
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Duncan Dickinson
 */
public class GHLicenseTest extends AbstractGitHubWireMockTest {

    /**
     * Basic test to ensure that the list of licenses from {@link GitHub#listLicenses()} is returned
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void listLicenses() throws IOException {
        Iterable<GHLicense> licenses = gitHub.listLicenses();
        assertTrue(licenses.iterator().hasNext());
    }

    /**
     * Tests that {@link GitHub#listLicenses()} returns the MIT license in the expected manner.
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void listLicensesCheckIndividualLicense() throws IOException {
        PagedIterable<GHLicense> licenses = gitHub.listLicenses();
        for (GHLicense lic : licenses) {
            if (lic.getKey().equals("mit")) {
                assertTrue(lic.getUrl().equals(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
                return;
            }
        }
        fail("The MIT license was not found");
    }

    /**
     * Checks that the request for an individual license using {@link GitHub#getLicense(String)} returns expected values
     * (not all properties are checked)
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void getLicense() throws IOException {
        String key = "mit";
        GHLicense license = gitHub.getLicense(key);
        assertNotNull(license);
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The HTML URL is correct",
                license.getHtmlUrl().equals(new URL("http://choosealicense.com/licenses/mit/")));
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)} and checks that the license is
     * correct
     *
     * @throws IOException
     *             if test failss
     */
    @Test
    public void checkRepositoryLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("github-api/github-api");
        GHLicense license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct",
                license.getUrl().equals(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
    }

    /**
     * Accesses the 'atom/atom' repo using {@link GitHub#getRepository(String)} and checks that the license is correct
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void checkRepositoryLicenseAtom() throws IOException {
        GHRepository repo = gitHub.getRepository("atom/atom");
        GHLicense license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct",
                license.getUrl().equals(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
    }

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)} and checks that the license is correct
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void checkRepositoryLicensePomes() throws IOException {
        GHRepository repo = gitHub.getRepository("pomes/pomes");
        GHLicense license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("apache-2.0"));
        assertTrue("The name is correct", license.getName().equals("Apache License 2.0"));
        assertTrue("The URL is correct",
                license.getUrl().equals(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/apache-2.0")));
    }

    /**
     * Accesses the 'dedickinson/test-repo' repo using {@link GitHub#getRepository(String)} and checks that *no* license
     * is returned as the repo doesn't have one
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void checkRepositoryWithoutLicense() throws IOException {
        GHRepository repo = gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty");
        GHLicense license = repo.getLicense();
        assertNull("There is no license", license);
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)} and then calls
     * {@link GHRepository#getLicense()} and checks that certain properties are correct
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void checkRepositoryFullLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("github-api/github-api");
        GHLicense license = repo.getLicense();
        assertNotNull("The license is populated", license);
        assertTrue("The key is correct", license.getKey().equals("mit"));
        assertTrue("The name is correct", license.getName().equals("MIT License"));
        assertTrue("The URL is correct",
                license.getUrl().equals(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
        assertTrue("The HTML URL is correct",
                license.getHtmlUrl().equals(new URL("http://choosealicense.com/licenses/mit/")));
    }

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)} and then calls
     * {@link GHRepository#getLicenseContent()} and checks that certain properties are correct
     *
     * @throws IOException
     *             if test fails
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
}

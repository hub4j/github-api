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

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHLicenseTest.
 *
 * @author Duncan Dickinson
 */
public class GHLicenseTest extends AbstractGitHubWireMockTest {

    /**
     * Basic test to ensure that the list of licenses from {@link GitHub#listLicenses()} is returned.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void listLicenses() throws IOException {
        Iterable<GHLicense> licenses = gitHub.listLicenses();
        assertThat(licenses, is(not(emptyIterable())));
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
                assertThat(lic.getUrl(), equalTo(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
                return;
            }
        }
        fail("The MIT license was not found");
    }

    /**
     * Checks that the request for an individual license using {@link GitHub#getLicense(String)} returns expected values
     * (not all properties are checked).
     *
     * @return the license
     * @throws IOException             if test fails
     */
    @Test
    public void getLicense() throws IOException {
        String key = "mit";
        GHLicense license = gitHub.getLicense(key);
        assertThat(license, notNullValue());
        assertThat("The name is correct", license.getName(), equalTo("MIT License"));
        assertThat("The HTML URL is correct",
                license.getHtmlUrl(),
                equalTo(new URL("http://choosealicense.com/licenses/mit/")));
        assertThat(license.getBody(), startsWith("MIT License\n" + "\n" + "Copyright (c) [year] [fullname]\n\n"));
        assertThat(license.getForbidden(), is(empty()));
        assertThat(license.getPermitted(), is(empty()));
        assertThat(license.getRequired(), is(empty()));
        assertThat(license.getImplementation(),
                equalTo("Create a text file (typically named LICENSE or LICENSE.txt) in the root of your source code and copy the text of the license into the file. Replace [year] with the current year and [fullname] with the name (or names) of the copyright holders."));
        assertThat(license.getCategory(), nullValue());
        assertThat(license.isFeatured(), equalTo(true));
        assertThat(license.equals(null), equalTo(false));
        assertThat(license.equals(gitHub.getLicense(key)), equalTo(true));
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)} and checks that the license is
     * correct.
     *
     * @throws IOException             if test failss
     */
    @Test
    public void checkRepositoryLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");
        GHLicense license = repo.getLicense();
        assertThat("The license is populated", license, notNullValue());
        assertThat("The key is correct", license.getKey(), equalTo("mit"));
        assertThat("The name is correct", license.getName(), equalTo("MIT License"));
        assertThat("The URL is correct",
                license.getUrl(),
                equalTo(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
    }

    /**
     * Accesses the 'atom/atom' repo using {@link GitHub#getRepository(String)} and checks that the license is correct.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void checkRepositoryLicenseAtom() throws IOException {
        GHRepository repo = gitHub.getRepository("atom/atom");
        GHLicense license = repo.getLicense();
        assertThat("The license is populated", license, notNullValue());
        assertThat("The key is correct", license.getKey(), equalTo("mit"));
        assertThat("The name is correct", license.getName(), equalTo("MIT License"));
        assertThat("The URL is correct",
                license.getUrl(),
                equalTo(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
    }

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)} and checks that the license is correct.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void checkRepositoryLicensePomes() throws IOException {
        GHRepository repo = gitHub.getRepository("pomes/pomes");
        GHLicense license = repo.getLicense();
        assertThat("The license is populated", license, notNullValue());
        assertThat("The key is correct", license.getKey(), equalTo("apache-2.0"));
        assertThat("The name is correct", license.getName(), equalTo("Apache License 2.0"));
        assertThat("The URL is correct",
                license.getUrl(),
                equalTo(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/apache-2.0")));
    }

    /**
     * Accesses the 'dedickinson/test-repo' repo using {@link GitHub#getRepository(String)} and checks that *no* license
     * is returned as the repo doesn't have one.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void checkRepositoryWithoutLicense() throws IOException {
        GHRepository repo = gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty");
        GHLicense license = repo.getLicense();
        assertThat("There is no license", license, nullValue());
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)} and then calls
     * {@link GHRepository#getLicense()} and checks that certain properties are correct.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void checkRepositoryFullLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");
        GHLicense license = repo.getLicense();
        assertThat("The license is populated", license, notNullValue());
        assertThat("The key is correct", license.getKey(), equalTo("mit"));
        assertThat("The name is correct", license.getName(), equalTo("MIT License"));
        assertThat("The URL is correct",
                license.getUrl(),
                equalTo(new URL(mockGitHub.apiServer().baseUrl() + "/licenses/mit")));
        assertThat("The HTML URL is correct",
                license.getHtmlUrl(),
                equalTo(new URL("http://choosealicense.com/licenses/mit/")));
    }

    /**
     * Accesses the 'pomes/pomes' repo using {@link GitHub#getRepository(String)} and then calls
     * {@link GHRepository#getLicenseContent()} and checks that certain properties are correct.
     *
     * @throws IOException             if test fails
     */
    @Test
    public void checkRepositoryLicenseContent() throws IOException {
        GHRepository repo = gitHub.getRepository("pomes/pomes");
        GHContent content = repo.getLicenseContent();
        assertThat("The license content is populated", content, notNullValue());
        assertThat("The type is 'file'", content.getType(), equalTo("file"));
        assertThat("The license file is 'LICENSE'", content.getName(), equalTo("LICENSE"));

        if (content.getEncoding().equals("base64")) {
            String licenseText = new String(IOUtils.toByteArray(content.read()));
            assertThat("The license appears to be an Apache License", licenseText.contains("Apache License"));
        } else {
            fail("Expected the license to be Base64 encoded but instead it was " + content.getEncoding());
        }
    }

    /**
     * Accesses the 'bndtools/bnd' repo using {@link GitHub#getRepository(String)} and then calls
     * {@link GHRepository#getLicense()}. The description is null due to multiple licences
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void checkRepositoryLicenseForIndeterminate() throws IOException {
        GHRepository repo = gitHub.getRepository("bndtools/bnd");
        GHLicense license = repo.getLicense();
        assertThat("The license is populated", license, notNullValue());
        assertThat(license.getKey(), equalTo("other"));
        assertThat(license.getDescription(), is(nullValue()));
        assertThat(license.getUrl(), is(nullValue()));
    }
}

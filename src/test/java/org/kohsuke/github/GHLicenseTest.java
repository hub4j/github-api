/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 * @author Duncan Dickinson
 */
public class GHLicenseTest extends Assert {
    private GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        gitHub = new GitHubBuilder()
                .fromCredentials()
                .build();
    }

    /**
     * Basic test to ensure that the list of licenses from {@link GitHub#listLicenses()} is returned
     *
     * @throws IOException
     */
    @Test
    public void listLicenses() throws IOException {
        Iterable<GHLicense> licenses = gitHub.listLicenses();
        assertTrue(licenses.iterator().hasNext());
    }

    /**
     * Tests that {@link GitHub#listLicenses()} returns the MIT license
     * in the expected manner.
     *
     * @throws IOException
     */
    @Test
    public void listLicensesCheckIndividualLicense() throws IOException {
        PagedIterable<GHLicense> licenses = gitHub.listLicenses();
        for (GHLicense lic : licenses) {
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
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)}
     * and checks that the license is correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("kohsuke/github-api");
        GHLicense license = repo.getLicense();
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
        GHLicense license = repo.getLicense();
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
        GHLicense license = repo.getLicense();
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
        GHLicense license = repo.getLicense();
        assertNull("There is no license", license);
    }

    /**
     * Accesses the 'kohsuke/github-api' repo using {@link GitHub#getRepository(String)}
     * and then calls {@link GHRepository#getLicense()} and checks that certain
     * properties are correct
     *
     * @throws IOException
     */
    @Test
    public void checkRepositoryFullLicense() throws IOException {
        GHRepository repo = gitHub.getRepository("kohsuke/github-api");
        GHLicense license = repo.getLicense();
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
}

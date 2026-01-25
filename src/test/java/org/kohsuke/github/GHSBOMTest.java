package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Tests for the SBOM (Software Bill of Materials) API.
 *
 * @see GHRepository#getSBOM()
 * @see <a href="https://docs.github.com/en/rest/dependency-graph/sboms">GitHub SBOM API</a>
 */
public class GHSBOMTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHSBOMTest instance.
     */
    public GHSBOMTest() {
    }

    /**
     * Tests that the SBOM for a repository can be retrieved and has expected structure.
     *
     * @throws IOException
     *             if test fails
     */
    @Test
    public void getSBOM() throws IOException {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");
        GHSBOMExportResult result = repo.getSBOM();

        assertThat("The SBOM result is populated", result, notNullValue());

        GHSBOM sbom = result.getSbom();
        assertThat("The SBOM is populated", sbom, notNullValue());

        assertThat("The SPDX ID is correct", sbom.getSPDXID(), equalTo("SPDXRef-DOCUMENT"));
        assertThat("The SPDX version is correct", sbom.getSpdxVersion(), equalTo("SPDX-2.3"));
        assertThat("The document name is correct", sbom.getName(), equalTo("com.github.hub4j/github-api"));
        assertThat("The data license is CC0-1.0", sbom.getDataLicense(), equalTo("CC0-1.0"));
        assertThat("The document namespace is set", sbom.getDocumentNamespace(), notNullValue());

        GHSBOM.CreationInfo creationInfo = sbom.getCreationInfo();
        assertThat("The creation info is populated", creationInfo, notNullValue());
        assertThat("The created timestamp is set", creationInfo.getCreated(), notNullValue());
        assertThat("The creators list is not empty", creationInfo.getCreators(), not(empty()));
        assertThat("GitHub is listed as creator",
                creationInfo.getCreators(),
                hasItem(containsString("GitHub.com-Dependency-Graph")));

        // documentDescribes is not present in all responses
        assertThat("getDocumentDescribes returns null when not present", sbom.getDocumentDescribes(), nullValue());

        List<GHSBOM.Package> packages = sbom.getPackages();
        assertThat("The packages list is not empty", packages, not(empty()));

        GHSBOM.Package firstPackage = packages.get(0);
        assertThat("The first package has an SPDX ID", firstPackage.getSPDXID(), notNullValue());
        assertThat("The first package has a name", firstPackage.getName(), notNullValue());
        assertThat("Package has downloadLocation", firstPackage.getDownloadLocation(), notNullValue());
        assertThat("Package filesAnalyzed is accessible", firstPackage.isFilesAnalyzed(), is(false));

        // Find package with version info, license, and copyright (hamcrest-library with version 3.0)
        GHSBOM.Package hamcrestPkg = packages.stream()
                .filter(p -> p.getName().contains("hamcrest-library") && "3.0".equals(p.getVersionInfo()))
                .findFirst()
                .orElse(null);
        assertThat("Found hamcrest-library package", hamcrestPkg, notNullValue());
        assertThat("Package has versionInfo", hamcrestPkg.getVersionInfo(), equalTo("3.0"));
        assertThat("Package has licenseConcluded", hamcrestPkg.getLicenseConcluded(), equalTo("BSD-3-Clause"));
        assertThat("Package has copyrightText", hamcrestPkg.getCopyrightText(), containsString("hamcrest.org"));

        // Find package with licenseDeclared (hub4j/github-api)
        GHSBOM.Package hub4jPkg = packages.stream()
                .filter(p -> "com.github.hub4j/github-api".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat("Found hub4j/github-api package", hub4jPkg, notNullValue());
        assertThat("Package has licenseDeclared", hub4jPkg.getLicenseDeclared(), equalTo("MIT"));

        // supplier is not present in this response
        assertThat("getSupplier returns null when not present", firstPackage.getSupplier(), nullValue());

        boolean foundPackageWithExternalRefs = false;
        for (GHSBOM.Package pkg : packages) {
            if (!pkg.getExternalRefs().isEmpty()) {
                foundPackageWithExternalRefs = true;
                GHSBOM.ExternalRef ref = pkg.getExternalRefs().get(0);
                assertThat("External ref has a category", ref.getReferenceCategory(), notNullValue());
                assertThat("External ref has a type", ref.getReferenceType(), notNullValue());
                assertThat("External ref has a locator", ref.getReferenceLocator(), notNullValue());
                break;
            }
        }
        assertThat("At least one package has external refs", foundPackageWithExternalRefs, is(true));

        List<GHSBOM.Relationship> relationships = sbom.getRelationships();
        assertThat("The relationships list is not empty", relationships, not(empty()));

        GHSBOM.Relationship firstRelationship = relationships.get(0);
        assertThat("The first relationship has a type", firstRelationship.getRelationshipType(), notNullValue());
        assertThat("The first relationship has an element ID", firstRelationship.getSpdxElementId(), notNullValue());
        assertThat("The first relationship has a related element",
                firstRelationship.getRelatedSpdxElement(),
                notNullValue());
    }
}

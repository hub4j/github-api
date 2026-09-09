package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

/**
 * Represents an SPDX Software Bill of Materials (SBOM) for a repository.
 *
 * @see GHRepository#getSBOM()
 * @see <a href="https://docs.github.com/en/rest/dependency-graph/sboms">GitHub SBOM API</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHSBOM {

    /**
     * Represents the creation information for an SBOM.
     */
    public static class CreationInfo {

        private String created;
        private List<String> creators;

        /**
         * Create default CreationInfo instance.
         */
        public CreationInfo() {
        }

        /**
         * Gets the creation timestamp.
         *
         * @return the creation timestamp in ISO 8601 format
         */
        public String getCreated() {
            return created;
        }

        /**
         * Gets the list of creators.
         *
         * @return the list of creators (e.g., "Tool: GitHub.com-Dependency-Graph")
         */
        public List<String> getCreators() {
            return creators != null ? Collections.unmodifiableList(creators) : Collections.emptyList();
        }
    }

    /**
     * Represents an external reference for a package.
     */
    public static class ExternalRef {

        @JsonProperty("referenceCategory")
        private String referenceCategory;
        @JsonProperty("referenceLocator")
        private String referenceLocator;
        @JsonProperty("referenceType")
        private String referenceType;

        /**
         * Create default ExternalRef instance.
         */
        public ExternalRef() {
        }

        /**
         * Gets the reference category.
         *
         * @return the reference category (e.g., "PACKAGE-MANAGER")
         */
        public String getReferenceCategory() {
            return referenceCategory;
        }

        /**
         * Gets the reference locator.
         *
         * @return the reference locator in PURL format
         */
        public String getReferenceLocator() {
            return referenceLocator;
        }

        /**
         * Gets the reference type.
         *
         * @return the reference type (e.g., "purl")
         */
        public String getReferenceType() {
            return referenceType;
        }
    }

    /**
     * Represents a package in the SBOM.
     */
    public static class Package {

        @JsonProperty("copyrightText")
        private String copyrightText;
        @JsonProperty("downloadLocation")
        private String downloadLocation;
        @JsonProperty("externalRefs")
        private List<ExternalRef> externalRefs;
        @JsonProperty("filesAnalyzed")
        private boolean filesAnalyzed;
        @JsonProperty("licenseConcluded")
        private String licenseConcluded;
        @JsonProperty("licenseDeclared")
        private String licenseDeclared;
        private String name;
        @JsonProperty("SPDXID")
        private String spdxid;
        private String supplier;
        @JsonProperty("versionInfo")
        private String versionInfo;

        /**
         * Create default Package instance.
         */
        public Package() {
        }

        /**
         * Gets the copyright text.
         *
         * @return the copyright text, or null if not specified
         */
        @CheckForNull
        public String getCopyrightText() {
            return copyrightText;
        }

        /**
         * Gets the download location.
         *
         * @return the download location
         */
        public String getDownloadLocation() {
            return downloadLocation;
        }

        /**
         * Gets the external references.
         *
         * @return the external references
         */
        public List<ExternalRef> getExternalRefs() {
            return externalRefs != null ? Collections.unmodifiableList(externalRefs) : Collections.emptyList();
        }

        /**
         * Gets the concluded license.
         *
         * @return the concluded license, or null if not specified
         */
        @CheckForNull
        public String getLicenseConcluded() {
            return licenseConcluded;
        }

        /**
         * Gets the declared license.
         *
         * @return the declared license, or null if not specified
         */
        @CheckForNull
        public String getLicenseDeclared() {
            return licenseDeclared;
        }

        /**
         * Gets the package name.
         *
         * @return the package name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the SPDX identifier.
         *
         * @return the SPDX identifier
         */
        public String getSPDXID() {
            return spdxid;
        }

        /**
         * Gets the supplier.
         *
         * @return the supplier, or null if not specified
         */
        @CheckForNull
        public String getSupplier() {
            return supplier;
        }

        /**
         * Gets the version info.
         *
         * @return the version info, or null if not specified
         */
        @CheckForNull
        public String getVersionInfo() {
            return versionInfo;
        }

        /**
         * Returns whether files were analyzed.
         *
         * @return true if files were analyzed
         */
        public boolean isFilesAnalyzed() {
            return filesAnalyzed;
        }
    }

    /**
     * Represents a relationship between SPDX elements.
     */
    public static class Relationship {

        @JsonProperty("relatedSpdxElement")
        private String relatedSpdxElement;
        @JsonProperty("relationshipType")
        private String relationshipType;
        @JsonProperty("spdxElementId")
        private String spdxElementId;

        /**
         * Create default Relationship instance.
         */
        public Relationship() {
        }

        /**
         * Gets the related SPDX element.
         *
         * @return the related SPDX element ID
         */
        public String getRelatedSpdxElement() {
            return relatedSpdxElement;
        }

        /**
         * Gets the relationship type.
         *
         * @return the relationship type (e.g., "DEPENDS_ON")
         */
        public String getRelationshipType() {
            return relationshipType;
        }

        /**
         * Gets the SPDX element ID.
         *
         * @return the SPDX element ID
         */
        public String getSpdxElementId() {
            return spdxElementId;
        }
    }

    @JsonProperty("creationInfo")
    private CreationInfo creationInfo;
    @JsonProperty("dataLicense")
    private String dataLicense;
    @JsonProperty("documentDescribes")
    private String documentDescribes;
    @JsonProperty("documentNamespace")
    private String documentNamespace;
    private String name;
    private List<Package> packages;
    private List<Relationship> relationships;
    @JsonProperty("spdxVersion")
    private String spdxVersion;
    @JsonProperty("SPDXID")
    private String spdxid;

    /**
     * Create default GHSBOM instance.
     */
    public GHSBOM() {
    }

    /**
     * Gets the creation info.
     *
     * @return the creation info
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public CreationInfo getCreationInfo() {
        return creationInfo;
    }

    /**
     * Gets the data license.
     *
     * @return the data license (typically "CC0-1.0")
     */
    public String getDataLicense() {
        return dataLicense;
    }

    /**
     * Gets the document describes field.
     *
     * @return the document describes field, or null if not specified
     */
    @CheckForNull
    public String getDocumentDescribes() {
        return documentDescribes;
    }

    /**
     * Gets the document namespace.
     *
     * @return the document namespace URI
     */
    public String getDocumentNamespace() {
        return documentNamespace;
    }

    /**
     * Gets the document name.
     *
     * @return the document name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of packages.
     *
     * @return the list of packages
     */
    public List<Package> getPackages() {
        return packages != null ? Collections.unmodifiableList(packages) : Collections.emptyList();
    }

    /**
     * Gets the relationships.
     *
     * @return the relationships between SPDX elements
     */
    public List<Relationship> getRelationships() {
        return relationships != null ? Collections.unmodifiableList(relationships) : Collections.emptyList();
    }

    /**
     * Gets the SPDX identifier.
     *
     * @return the SPDX identifier (typically "SPDXRef-DOCUMENT")
     */
    public String getSPDXID() {
        return spdxid;
    }

    /**
     * Gets the SPDX version.
     *
     * @return the SPDX version (e.g., "SPDX-2.3")
     */
    public String getSpdxVersion() {
        return spdxVersion;
    }
}

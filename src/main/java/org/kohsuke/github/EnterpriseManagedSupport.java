package org.kohsuke.github;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility class for helping with operations for enterprise managed resources.
 *
 * @author Miguel Esteban Gutiérrez
 */
class EnterpriseManagedSupport {

    static final String COULD_NOT_RETRIEVE_ORGANIZATION_EXTERNAL_GROUPS = "Could not retrieve organization external groups";
    static final String NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR = "This organization is not part of externally managed enterprise.";
    static final String TEAM_CANNOT_BE_EXTERNALLY_MANAGED_ERROR = "This team cannot be externally managed since it has explicit members.";

    private static final Logger LOGGER = Logger.getLogger(EnterpriseManagedSupport.class.getName());

    private final GHOrganization organization;

    private EnterpriseManagedSupport(GHOrganization organization) {
        this.organization = organization;
    }

    Optional<GHIOException> filterException(final HttpException he, final String scenario) {
        if (he.getResponseCode() == 400) {
            final String responseMessage = he.getMessage();
            try {
                final GHError error = GitHubClient.getMappingObjectReader(this.organization.root())
                        .forType(GHError.class)
                        .readValue(responseMessage);
                if (NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR.equals(error.getMessage())) {
                    return Optional.of(new GHNotExternallyManagedEnterpriseException(scenario, error, he));
                } else if (TEAM_CANNOT_BE_EXTERNALLY_MANAGED_ERROR.equals(error.getMessage())) {
                    return Optional.of(new GHTeamCannotBeExternallyManagedException(scenario, error, he));
                }
            } catch (final JsonProcessingException e) {
                // We can ignore it
                LOGGER.warning(() -> logUnexpectedFailure(e, responseMessage));
            }
        }
        return Optional.empty();
    }

    Optional<GHException> filterException(final GHException e) {
        if (e.getCause() instanceof HttpException) {
            final HttpException he = (HttpException) e.getCause();
            return filterException(he, COULD_NOT_RETRIEVE_ORGANIZATION_EXTERNAL_GROUPS)
                    .map(translated -> new GHException(COULD_NOT_RETRIEVE_ORGANIZATION_EXTERNAL_GROUPS, translated));
        }
        return Optional.empty();
    }

    static EnterpriseManagedSupport forOrganization(final GHOrganization org) {
        return new EnterpriseManagedSupport(org);
    }

    private static String logUnexpectedFailure(final JsonProcessingException exception, final String payload) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return String.format("Could not parse GitHub error response: '%s'. Full stacktrace follows:%n%s", payload, sw);
    }

}

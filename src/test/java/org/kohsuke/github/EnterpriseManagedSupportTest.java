package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc

/**
 * The Class EnterpriseManagedSupportTest.
 */
public class EnterpriseManagedSupportTest extends AbstractGitHubWireMockTest {

    private static final String NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR = "{\"message\":\"This organization is not part of externally managed enterprise.\","
            + "\"documentation_url\": \"https://docs.github.com/rest/teams/external-groups#list-external-groups-in-an-organization\"}";

    private static final String UNKNOWN_ERROR = "{\"message\":\"Unknown error\","
            + "\"documentation_url\": \"https://docs.github.com/rest/unknown#unknown\"}";

    private static final String TEAM_CANNOT_BE_EXTERNALLY_MANAGED_ERROR = "{\"message\":\"This team cannot be externally managed since it has explicit members.\","
            + "\"documentation_url\": \"https://docs.github.com/rest/teams/external-groups#list-a-connection-between-an-external-group-and-a-team\"}";

    /**
     * Test to ensure that only HttpExceptions are handled
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testIgnoreNonHttpException() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final GHException inputCause = new GHException("Cause");
        final GHException inputException = new GHException("Test", inputCause);

        final Optional<GHException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException);

        assertThat(maybeException.isPresent(), is(false));
    }

    /**
     * Test to ensure that only BadRequests HttpExceptions are handled
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testIgnoreNonBadRequestExceptions() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final HttpException inputCause = new HttpException(NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR,
                404,
                "Error",
                org.getUrl().toString());
        final GHException inputException = new GHException("Test", inputCause);

        final Optional<GHException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException);

        assertThat(maybeException.isPresent(), is(false));
    }

    /**
     * Test to ensure that only BadRequests HttpExceptions with parseable JSON payload are handled
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testIgnoreBadRequestsWithUnparseableJson() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final HttpException inputCause = new HttpException("Error", 400, "Error", org.getUrl().toString());
        final GHException inputException = new GHException("Test", inputCause);

        final Optional<GHException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException);

        assertThat(maybeException.isPresent(), is(false));
    }

    /**
     * Test to ensure that only BadRequests HttpExceptions with known error message are handled
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testIgnoreBadRequestsWithUnknownErrorMessage() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final HttpException inputCause = new HttpException(UNKNOWN_ERROR, 400, "Error", org.getUrl().toString());
        final GHException inputException = new GHException("Test", inputCause);

        final Optional<GHException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException);

        assertThat(maybeException.isPresent(), is(false));
    }

    /**
     * Test to validate compliant use case.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testHandleEmbeddedNotPartOfExternallyManagedEnterpriseHttpException() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final HttpException inputCause = new HttpException(NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR,
                400,
                "Error",
                org.getUrl().toString());
        final GHException inputException = new GHException("Test", inputCause);

        final Optional<GHException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException);

        assertThat(maybeException.isPresent(), is(true));

        final GHException exception = maybeException.get();

        assertThat(exception.getMessage(),
                equalTo(EnterpriseManagedSupport.COULD_NOT_RETRIEVE_ORGANIZATION_EXTERNAL_GROUPS));

        final Throwable cause = exception.getCause();

        assertThat(cause, instanceOf(GHNotExternallyManagedEnterpriseException.class));

        final GHNotExternallyManagedEnterpriseException failure = (GHNotExternallyManagedEnterpriseException) cause;

        assertThat(failure.getCause(), is(inputCause));
        assertThat(failure.getMessage(),
                equalTo(EnterpriseManagedSupport.COULD_NOT_RETRIEVE_ORGANIZATION_EXTERNAL_GROUPS));

        final GHError error = failure.getError();

        assertThat(error, notNullValue());
        assertThat(error.getMessage(),
                equalTo(EnterpriseManagedSupport.NOT_PART_OF_EXTERNALLY_MANAGED_ENTERPRISE_ERROR));
        assertThat(error.getDocumentationUrl(), notNullValue());
    }

    /**
     * Test to validate another compliant use case.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testHandleTeamCannotBeExternallyManagedHttpException() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        final HttpException inputException = new HttpException(TEAM_CANNOT_BE_EXTERNALLY_MANAGED_ERROR,
                400,
                "Error",
                org.getUrl().toString());

        final Optional<GHIOException> maybeException = EnterpriseManagedSupport.forOrganization(org)
                .filterException(inputException, "Scenario");

        assertThat(maybeException.isPresent(), is(true));

        final GHIOException exception = maybeException.get();

        assertThat(exception.getMessage(), equalTo("Scenario"));
        assertThat(exception.getCause(), is(inputException));

        assertThat(exception, instanceOf(GHTeamCannotBeExternallyManagedException.class));

        final GHTeamCannotBeExternallyManagedException failure = (GHTeamCannotBeExternallyManagedException) exception;

        final GHError error = failure.getError();

        assertThat(error, notNullValue());
        assertThat(error.getMessage(), equalTo(EnterpriseManagedSupport.TEAM_CANNOT_BE_EXTERNALLY_MANAGED_ERROR));
        assertThat(error.getDocumentationUrl(), notNullValue());
    }
}

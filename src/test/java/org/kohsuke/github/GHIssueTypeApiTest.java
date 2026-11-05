package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the API request construction for issue types.
 */
public class GHIssueTypeApiTest {

    /**
     * Verifies that creating an issue can include an issue type.
     */
    @Test
    public void createsIssueWithType() {
        GitHub gitHub = mock(GitHub.class);
        Requester requester = mock(Requester.class, RETURNS_SELF);
        GHRepository repository = mock(GHRepository.class);
        when(gitHub.createRequest()).thenReturn(requester);
        doReturn(gitHub).when(repository).root();

        GHIssueBuilder builder = new GHIssueBuilder(repository, "Typed issue");

        assertThat(builder.type("Bug"), sameInstance(builder));
        assertThat(builder.type(null), sameInstance(builder));
        verify(requester).with("type", "Bug");
    }

    /**
     * Verifies the organization issue type endpoint and response type.
     */
    @Test
    public void listsOrganizationIssueTypes() {
        GitHub gitHub = mock(GitHub.class);
        Requester requester = mock(Requester.class, RETURNS_SELF);
        GHOrganization organization = mock(GHOrganization.class, CALLS_REAL_METHODS);
        @SuppressWarnings("unchecked")
        PagedIterable<GHIssueType> issueTypes = mock(PagedIterable.class);
        when(gitHub.createRequest()).thenReturn(requester);
        doReturn(gitHub).when(organization).root();
        organization.login = "hub4j";
        when(requester.toIterable(eq(GHIssueType[].class), isNull())).thenReturn(issueTypes);

        assertThat(organization.listIssueTypes(), sameInstance(issueTypes));
        verify(requester).withUrlPath("/orgs/hub4j/issue-types");
        verify(requester).toIterable(eq(GHIssueType[].class), isNull());
    }

    /**
     * Verifies that an existing issue type can be changed.
     *
     * @throws IOException
     *             if updating the issue fails
     */
    @Test
    public void setsIssueType() throws IOException {
        GitHub gitHub = mock(GitHub.class);
        Requester requester = mock(Requester.class, RETURNS_SELF);
        GHIssue issue = mock(GHIssue.class, CALLS_REAL_METHODS);
        when(gitHub.createRequest()).thenReturn(requester);
        doReturn(gitHub).when(issue).root();
        doReturn("/repos/hub4j/github-api/issues/2010").when(issue).getIssuesApiRoute();

        issue.setType("Bug");

        verify(requester).withNullable("type", "Bug");
        verify(requester).method("PATCH");
        verify(requester).withUrlPath("/repos/hub4j/github-api/issues/2010");
        verify(requester).send();
    }
}

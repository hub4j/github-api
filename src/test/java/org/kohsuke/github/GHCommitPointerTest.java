package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

// TODO: Auto-generated Javadoc
/**
 * The Class GHCommitPointerTest.
 */
public class GHCommitPointerTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHCommitPointerTest instance
     */
    public GHCommitPointerTest() {
    }

    /**
     * A head repository of JSON {@code null} throws {@link IOException} instead of {@link NullPointerException}.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCommitThrowsWhenRepositoryIsNull() throws Exception {
        snapshotNotAllowed();
        mockGitHub.apiServer()
                .stubFor(get(urlEqualTo("/repos/base-owner/base-repo")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"full_name\":\"base-owner/base-repo\"}")));
        mockGitHub.apiServer()
                .stubFor(get(urlEqualTo("/repos/base-owner/base-repo/pulls/1")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"head\":{\"sha\":\"1111111111111111111111111111111111111111\",\"repo\":null}}")));

        GHCommitPointer head = gitHub.getRepository("base-owner/base-repo").getPullRequest(1).getHead();
        assertThat(head.getRepository(), nullValue());

        IOException failure = assertThrows(IOException.class, head::getCommit);
        assertThat(failure.getMessage(),
                equalTo("Repository information is unavailable for this commit pointer"));
    }
}

package org.kohsuke.github;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.kohsuke.github.GHRepositoryTraffic.DailyInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class RepositoryTrafficTest.
 */
public class RepositoryTrafficTest extends AbstractGitHubWireMockTest {
    final private String repositoryName = "github-api";

    @SuppressWarnings("unchecked")
    private <T extends GHRepositoryTraffic> void checkResponse(T expected, T actual) {
        assertThat(actual.getCount(), Matchers.equalTo(expected.getCount()));
        assertThat(actual.getUniques(), Matchers.equalTo(expected.getUniques()));

        List<? extends DailyInfo> expectedList = expected.getDailyInfo();
        List<? extends DailyInfo> actualList = actual.getDailyInfo();
        Iterator<? extends DailyInfo> expectedIt;
        Iterator<? extends DailyInfo> actualIt;

        assertThat(actualList.size(), Matchers.equalTo(expectedList.size()));
        expectedIt = expectedList.iterator();
        actualIt = actualList.iterator();

        while (expectedIt.hasNext() && actualIt.hasNext()) {
            DailyInfo expectedDailyInfo = expectedIt.next();
            DailyInfo actualDailyInfo = actualIt.next();
            assertThat(actualDailyInfo.getCount(), Matchers.equalTo(expectedDailyInfo.getCount()));
            assertThat(actualDailyInfo.getUniques(), Matchers.equalTo(expectedDailyInfo.getUniques()));
            assertThat(actualDailyInfo.getTimestamp(), Matchers.equalTo(expectedDailyInfo.getTimestamp()));
        }
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j").getRepository("github-api");
    }

    /**
     * Test get views.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetViews() throws IOException {
        // Would change all the time
        snapshotNotAllowed();

        GHRepository repository = getRepository(gitHub);
        GHRepositoryViewTraffic views = repository.getViewTraffic();

        GHRepositoryViewTraffic expectedResult = new GHRepositoryViewTraffic(3533,
                616,
                Arrays.asList(new GHRepositoryViewTraffic.DailyInfo("2020-02-08T00:00:00Z", 101, 31),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-09T00:00:00Z", 92, 22),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-10T00:00:00Z", 317, 84),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-11T00:00:00Z", 365, 90),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-12T00:00:00Z", 428, 78),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-13T00:00:00Z", 334, 52),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-14T00:00:00Z", 138, 44),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-15T00:00:00Z", 76, 13),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-16T00:00:00Z", 99, 27),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-17T00:00:00Z", 367, 65),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-18T00:00:00Z", 411, 76),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-19T00:00:00Z", 140, 61),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-20T00:00:00Z", 259, 55),
                        new GHRepositoryViewTraffic.DailyInfo("2020-02-21T00:00:00Z", 406, 66)));
        checkResponse(expectedResult, views);
    }

    /**
     * Test get clones.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetClones() throws IOException {
        // Would change all the time
        snapshotNotAllowed();

        GHRepository repository = getRepository(gitHub);
        GHRepositoryCloneTraffic clones = repository.getCloneTraffic();

        GHRepositoryCloneTraffic expectedResult = new GHRepositoryCloneTraffic(128,
                25,
                Arrays.asList(new GHRepositoryCloneTraffic.DailyInfo("2020-02-08T00:00:00Z", 6, 3),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-10T00:00:00Z", 6, 4),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-11T00:00:00Z", 2, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-12T00:00:00Z", 1, 1),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-13T00:00:00Z", 1, 1),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-14T00:00:00Z", 2, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-15T00:00:00Z", 2, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-16T00:00:00Z", 2, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-17T00:00:00Z", 3, 3),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-18T00:00:00Z", 1, 1),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-20T00:00:00Z", 25, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2020-02-21T00:00:00Z", 77, 6)));
        checkResponse(expectedResult, clones);
    }

    /**
     * Test get traffic stats access failure due to insufficient permissions.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetTrafficStatsAccessFailureDueToInsufficientPermissions() throws IOException {
        // Snapshot taken without permissions
        snapshotNotAllowed();
        String errorMsg = "Exception should be thrown, since we don't have permission to access repo traffic info.";

        GHRepository repo = gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository(repositoryName);
        try {
            repo.getViewTraffic();
            fail(errorMsg);
        } catch (HttpException ex) {
        }
        try {
            repo.getCloneTraffic();
            fail(errorMsg);
        } catch (HttpException ex) {
        }
    }
}

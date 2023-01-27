package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.equalTo;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRepositoryStatisticsTest.
 */
public class GHRepositoryStatisticsTest extends AbstractGitHubWireMockTest {

    /** The max iterations. */
    public static int MAX_ITERATIONS = 3;

    /** The sleep interval. */
    public static int SLEEP_INTERVAL = 5000;

    /**
     * Test contributor stats.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    public void testContributorStats() throws IOException, InterruptedException {
        // get the statistics
        PagedIterable<GHRepositoryStatistics.ContributorStats> stats = getRepository().getStatistics()
                .getContributorStats();

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        List<GHRepositoryStatistics.ContributorStats> list = stats.toList();
        assertThat(list.size(), equalTo(99));

        // find a particular developer
        // TODO: Add an accessor method for this instead of having use a loop.
        boolean developerFound = false;
        final String authorLogin = "kohsuke";
        for (GHRepositoryStatistics.ContributorStats statsForAuthor : list) {
            if (authorLogin.equals(statsForAuthor.getAuthor().getLogin())) {
                assertThat(statsForAuthor.getTotal(), equalTo(715));
                assertThat(statsForAuthor.toString(), equalTo("kohsuke made 715 contributions over 494 weeks"));

                List<GHRepositoryStatistics.ContributorStats.Week> weeks = statsForAuthor.getWeeks();
                assertThat(weeks.size(), equalTo(494));

                try {
                    // check a particular week
                    // TODO: Maybe add a convenience method to get the week
                    // containing a certain date (Java.Util.Date).
                    GHRepositoryStatistics.ContributorStats.Week week = statsForAuthor.getWeek(1541289600);
                    assertThat(week.getNumberOfAdditions(), equalTo(63));
                    assertThat(week.getNumberOfDeletions(), equalTo(56));
                    assertThat(week.getNumberOfCommits(), equalTo(5));
                    assertThat(week.toString(),
                            equalTo("Week starting 1541289600 - Additions: 63, Deletions: 56, Commits: 5"));
                } catch (NoSuchElementException e) {
                    fail("Did not find week 1546128000");
                }
                developerFound = true;
                break;
            }
        }

        assertThat("Did not find author " + authorLogin, developerFound);
    }

    /**
     * Test commit activity.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCommitActivity() throws IOException, InterruptedException {
        // get the statistics
        PagedIterable<GHRepositoryStatistics.CommitActivity> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getCommitActivity();
            if (stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            } else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        List<GHRepositoryStatistics.CommitActivity> list = stats.toList();

        // TODO: Return this as a map with the timestamp as the key.
        // Either that or wrap in an object an accessor method.
        Boolean foundWeek = false;
        for (GHRepositoryStatistics.CommitActivity item : list) {
            if (item.getWeek() == 1566691200) {
                assertThat(item.getTotal(), equalTo(6));
                List<Integer> days = item.getDays();
                assertThat((long) days.get(0), equalTo(0L));
                assertThat((long) days.get(1), equalTo(0L));
                assertThat((long) days.get(2), equalTo(1L));
                assertThat((long) days.get(3), equalTo(0L));
                assertThat((long) days.get(4), equalTo(0L));
                assertThat((long) days.get(5), equalTo(1L));
                assertThat((long) days.get(6), equalTo(4L));
                foundWeek = true;
                break;
            }
        }
        assertThat("Could not find week starting 1546128000", foundWeek);
    }

    /**
     * Test code frequency.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCodeFrequency() throws IOException, InterruptedException {
        // get the statistics
        List<GHRepositoryStatistics.CodeFrequency> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getCodeFrequency();
            if (stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            } else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        // TODO: Perhaps return this as a map with the timestamp as the key?
        // Either that or wrap in an object with accessor methods.
        Boolean foundWeek = false;
        for (GHRepositoryStatistics.CodeFrequency item : stats) {
            if (item.getWeekTimestamp() == 1535241600) {
                assertThat(item.getAdditions(), equalTo(185L));
                assertThat(item.getDeletions(), equalTo(-243L));
                assertThat(item.toString(), equalTo("Week starting 1535241600 has 185 additions and 243 deletions"));
                foundWeek = true;
                break;
            }
        }
        assertThat("Could not find week starting 1535241600", foundWeek);
    }

    /**
     * Test participation.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    public void testParticipation() throws IOException, InterruptedException {
        // get the statistics
        GHRepositoryStatistics.Participation stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getParticipation();
            if (stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            } else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        List<Integer> allCommits = stats.getAllCommits();
        assertThat(allCommits.size(), equalTo(52));
        assertThat((int) allCommits.get(2), equalTo(2));

        List<Integer> ownerCommits = stats.getOwnerCommits();
        assertThat(ownerCommits.size(), equalTo(52));
        // The values depend on who is running the test.
    }

    /**
     * Test punch card.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testPunchCard() throws IOException, InterruptedException {
        // get the statistics
        List<GHRepositoryStatistics.PunchCardItem> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getPunchCard();
            if (stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            } else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        Boolean hourFound = false;
        for (GHRepositoryStatistics.PunchCardItem item : stats) {
            if (item.getDayOfWeek() == 2 && item.getHourOfDay() == 10) {
                // TODO: Make an easier access method. Perhaps wrap in an
                // object and have a method such as GetCommits(1, 16).
                assertThat(item.getNumberOfCommits(), equalTo(16L));
                assertThat(item.toString(), equalTo("Day 2 Hour 10: 16 commits"));
                hourFound = true;
                break;
            }
        }
        assertThat("Hour 10 for Day 2 not found.", hourFound);
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
    }
}

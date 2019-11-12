package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class GHRepositoryStatisticsTest extends AbstractGitHubWireMockTest {

    public static int MAX_ITERATIONS = 3;
    public static int SLEEP_INTERVAL = 5000;

    @Test
    public void testContributorStats() throws IOException, InterruptedException {
        // get the statistics
        PagedIterable<GHRepositoryStatistics.ContributorStats> stats =
                getRepository().getStatistics().getContributorStats();

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve2.");
            return;
        }

        // check the statistics are accurate
        List<GHRepositoryStatistics.ContributorStats> list = stats.asList();
        assertEquals(99, list.size());

        // find a particular developer
        // TODO: Add an accessor method for this instead of having use a loop.
        boolean developerFound = false;
        final String authorLogin = "kohsuke";
        for (GHRepositoryStatistics.ContributorStats statsForAuthor: list) {
            if (authorLogin.equals(statsForAuthor.getAuthor().getLogin())) {
                assertEquals(715, statsForAuthor.getTotal());

                List<GHRepositoryStatistics.ContributorStats.Week> weeks =
                        statsForAuthor.getWeeks();
                assertEquals(494, weeks.size());

                try {
                    // check a particular week
                    // TODO: Maybe add a convenience method to get the week
                    // containing a certain date (Java.Util.Date).
                    GHRepositoryStatistics.ContributorStats.Week week =
                            statsForAuthor.getWeek(1541289600);
                    assertEquals(63, week.getNumberOfAdditions());
                    assertEquals(56, week.getNumberOfDeletions());
                    assertEquals(5, week.getNumberOfCommits());
                }
                catch(NoSuchElementException e) {
                    fail("Did not find week 1546128000");
                }
                developerFound = true;
                break;
            }
        }

        assertTrue("Did not find author " + authorLogin, developerFound);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCommitActivity() throws IOException, InterruptedException {
        // get the statistics
        PagedIterable<GHRepositoryStatistics.CommitActivity> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getCommitActivity();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve2.");
            return;
        }

        // check the statistics are accurate
        List<GHRepositoryStatistics.CommitActivity> list = stats.asList();

        // TODO: Return this as a map with the timestamp as the key.
        // Either that or wrap in an object an accessor method.
        Boolean foundWeek = false;
        for (GHRepositoryStatistics.CommitActivity item: list) {
            if (item.getWeek() == 1566691200) {
                assertEquals(6, item.getTotal());
                List<Integer> days = item.getDays();
                assertEquals(0, (long)days.get(0));
                assertEquals(0, (long)days.get(1));
                assertEquals(1, (long)days.get(2));
                assertEquals(0, (long)days.get(3));
                assertEquals(0, (long)days.get(4));
                assertEquals(1, (long)days.get(5));
                assertEquals(4, (long)days.get(6));
                foundWeek = true;
                break;
            }
        }
        assertTrue("Could not find week starting 1546128000", foundWeek);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCodeFrequency() throws IOException, InterruptedException {
        // get the statistics
        List<GHRepositoryStatistics.CodeFrequency> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getCodeFrequency();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve2.");
            return;
        }

        // check the statistics are accurate
        // TODO: Perhaps return this as a map with the timestamp as the key?
        // Either that or wrap in an object with accessor methods.
        Boolean foundWeek = false;
        for (GHRepositoryStatistics.CodeFrequency item: stats) {
            if (item.getWeekTimestamp() == 1535241600) {
                assertEquals(185, item.getAdditions());
                assertEquals(-243, item.getDeletions());
                foundWeek = true;
                break;
            }
        }
        assertTrue("Could not find week starting 1535241600", foundWeek);
    }

    @Test
    public void testParticipation() throws IOException, InterruptedException {
        // get the statistics
        GHRepositoryStatistics.Participation stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = getRepository().getStatistics().getParticipation();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve2.");
            return;
        }

        // check the statistics are accurate
        List<Integer> allCommits = stats.getAllCommits();
        assertEquals(52, allCommits.size());
        assertEquals(2, (int)allCommits.get(2));

        List<Integer> ownerCommits = stats.getOwnerCommits();
        assertEquals(52, ownerCommits.size());
        // The values depend on who is running the test.
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testPunchCard() throws IOException, InterruptedException {
        // get the statistics
        List<GHRepositoryStatistics.PunchCardItem> stats = null;

        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
             stats = getRepository().getStatistics().getPunchCard();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve2.");
            return;
        }

        // check the statistics are accurate
        Boolean hourFound = false;
        for (GHRepositoryStatistics.PunchCardItem item: stats) {
            if(item.getDayOfWeek() == 2 && item.getHourOfDay() == 10) {
                // TODO: Make an easier access method. Perhaps wrap in an
                // object and have a method such as GetCommits(1, 16).
                assertEquals(16, item.getNumberOfCommits());
                hourFound = true;
                break;
            }
        }
        assertTrue("Hour 10 for Day 2 not found.", hourFound);
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
    }
}

package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class StatisticsTest extends AbstractGitHubApiTestBase {

    public static final String REPOSITORY = "martinvanzijl/sandbox";
    public static int MAX_ITERATIONS = 3;
    public static int SLEEP_INTERVAL = 5000;

    private GitHub github;
    private GHRepository repo;

    @Override
    public void setUp() throws Exception {
        github = GitHub.connect();
        repo = github.getRepository(REPOSITORY);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testContributorStats() throws IOException, InterruptedException {
        PagedIterable<GHRepository.ContributorStats> stats = null;

        // get statistics
        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = repo.getContributorStats();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        List<GHRepository.ContributorStats> list = stats.asList();

        assertEquals(1, list.size());

        GHRepository.ContributorStats statsForAuthor = list.get(0);

        assertEquals("martinvanzijl", statsForAuthor.getAuthor().getLogin());
        assertEquals(4, statsForAuthor.getTotal());

        List<GHRepository.ContributorStats.Week> weeks = statsForAuthor.getWeeks();
        assertNotNull(weeks);
        assertNotEquals(0, weeks.size());

        try {
            // check a particular week
            // TODO: Maybe add a convenience method to get the week containing
            // a certain date (Java.Util.Date).
            GHRepository.ContributorStats.Week week =
                    statsForAuthor.getWeek(1546128000);
            assertEquals(1, week.getNumberOfAdditions());
            assertEquals(0, week.getNumberOfDeletions());
            assertEquals(1, week.getNumberOfCommits());
        }
        catch(NoSuchElementException e) {
            fail("Did not find week 1546128000");
        }
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCommitActivity() throws IOException, InterruptedException {
        PagedIterable<GHRepository.CommitActivity> stats = null;

        // get statistics
        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = repo.getCommitActivity();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                break;
            }
        }

        // check that the statistics were eventually retrieved
        if (stats == null) {
            fail("Statistics took too long to retrieve.");
            return;
        }

        // check the statistics are accurate
        List<GHRepository.CommitActivity> list = stats.asList();

        // TODO: Perhaps return this as a map with the timestamp as the key?
        // Either that or wrap in an object with accessor methods.
        Boolean foundWeek = false;
        for (GHRepository.CommitActivity item: list) {
            if (item.getWeek() == 1546128000) {
                assertEquals(1, item.getTotal());
                List<Integer> days = item.getDays();
                assertEquals(0, (long)days.get(0));
                assertEquals(1, (long)days.get(1));
                foundWeek = true;
                break;
            }
        }
        assertTrue("Could not find week starting 1546128000", foundWeek);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCodeFrequency() throws IOException, InterruptedException {
        List<GHRepository.CodeFrequency> stats = null;

        // get statistics
        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = repo.getCodeFrequency();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
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
        for (GHRepository.CodeFrequency item: stats) {
            if (item.getWeekTimestamp() == 1546128000) {
                assertEquals(1, item.getAdditions());
                assertEquals(0, item.getDeletions());
                foundWeek = true;
                break;
            }
        }
        assertTrue("Could not find week starting 1546128000", foundWeek);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testParticipation() throws IOException, InterruptedException {
        GHRepository.Participation stats = null;

        // get statistics
        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            stats = repo.getParticipation();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
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
        assertEquals(52, allCommits.size());
        // TODO: Create a temporary repository and perform a certain number
        // of commits, then check the first (only) item in the list.

        List<Integer> ownerCommits = stats.getOwnerCommits();
        assertEquals(52, ownerCommits.size());
        // TODO: Same as above.
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testPunchCard() throws IOException, InterruptedException {
        List<GHRepository.PunchCardItem> stats = null;

        // get statistics
        for (int i = 0; i < MAX_ITERATIONS; i += 1) {
             stats = repo.getPunchCard();
            if(stats == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
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
        for (GHRepository.PunchCardItem item: stats) {
            if(item.getDayOfWeek() == 1 && item.getHourOfDay() == 16) {
                // TODO: Make an easier access method. Perhaps wrap in an
                // object and have a method such as GetCommits(1, 16).
                assertEquals(1, item.getNumberOfCommits());
                hourFound = true;
                break;
            }
        }
        assertTrue("Hour 16 for Day 1 not found.", hourFound);
    }
}

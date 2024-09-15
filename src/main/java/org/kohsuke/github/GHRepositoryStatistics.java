package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// TODO: Auto-generated Javadoc
/**
 * Statistics for a GitHub repository.
 *
 * @author Martin van Zijl
 */
public class GHRepositoryStatistics extends GitHubInteractiveObject {

    private final GHRepository repo;

    private static final int MAX_WAIT_ITERATIONS = 3;
    private static final int WAIT_SLEEP_INTERVAL = 5000;

    /**
     * Instantiates a new Gh repository statistics.
     *
     * @param repo
     *            the repo
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Acceptable risk")
    public GHRepositoryStatistics(GHRepository repo) {
        super(repo.root());
        this.repo = repo;
    }

    /**
     * Get contributors list with additions, deletions, and commit count. See
     * https://developer.github.com/v3/repos/statistics/#get-contributors-list-with-additions-deletions-and-commit-counts
     *
     * @return the contributor stats
     * @throws IOException
     *             the io exception
     * @throws InterruptedException
     *             the interrupted exception
     */
    public PagedIterable<ContributorStats> getContributorStats() throws IOException, InterruptedException {
        return getContributorStats(true);
    }

    /**
     * Gets contributor stats.
     *
     * @param waitTillReady
     *            Whether to sleep the thread if necessary until the statistics are ready. This is true by default.
     * @return the contributor stats
     * @throws IOException
     *             the io exception
     * @throws InterruptedException
     *             the interrupted exception
     */
    @BetaApi
    @SuppressWarnings("SleepWhileInLoop")
    @SuppressFBWarnings(value = { "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" }, justification = "JSON API")
    public PagedIterable<ContributorStats> getContributorStats(boolean waitTillReady)
            throws IOException, InterruptedException {
        PagedIterable<GHRepositoryStatistics.ContributorStats> stats = getContributorStatsImpl();

        if (stats == null && waitTillReady) {
            for (int i = 0; i < MAX_WAIT_ITERATIONS; i += 1) {
                // Wait a few seconds and try again.
                Thread.sleep(WAIT_SLEEP_INTERVAL);
                stats = getContributorStatsImpl();
                if (stats != null) {
                    break;
                }
            }
        }

        return stats;
    }

    /**
     * This gets the actual statistics from the server. Returns null if they are still being cached.
     */
    private PagedIterable<ContributorStats> getContributorStatsImpl() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("contributors"))
                .toIterable(ContributorStats[].class, null);
    }

    /**
     * The type ContributorStats.
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                    "URF_UNREAD_FIELD" },
            justification = "JSON API")
    public static class ContributorStats extends GHObject {
        private GHUser author;
        private int total;
        private List<Week> weeks;

        /**
         * Gets author.
         *
         * @return The author described by these statistics.
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
        public GHUser getAuthor() {
            return author;
        }

        /**
         * Gets total.
         *
         * @return The total number of commits authored by the contributor.
         */
        public int getTotal() {
            return total;
        }

        /**
         * Convenience method to look up week with particular timestamp.
         *
         * @param timestamp
         *            The timestamp to look for.
         * @return The week starting with the given timestamp. Throws an exception if it is not found.
         * @throws NoSuchElementException
         *             the no such element exception
         */
        public Week getWeek(long timestamp) throws NoSuchElementException {
            // maybe store the weeks in a map to make this more efficient?
            for (Week week : weeks) {
                if (week.getWeekTimestamp() == timestamp) {
                    return week;
                }
            }

            // this is safer than returning null
            throw new NoSuchElementException();
        }

        /**
         * Gets weeks.
         *
         * @return The total number of commits authored by the contributor.
         */
        public List<Week> getWeeks() {
            return Collections.unmodifiableList(weeks);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return author.getLogin() + " made " + String.valueOf(total) + " contributions over "
                    + String.valueOf(weeks.size()) + " weeks";
        }

        /**
         * The type Week.
         */
        @SuppressFBWarnings(
                value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                        "URF_UNREAD_FIELD" },
                justification = "JSON API")
        public static class Week {

            private long w;
            private int a;
            private int d;
            private int c;

            /**
             * Gets week timestamp.
             *
             * @return Start of the week, as a UNIX timestamp.
             */
            public long getWeekTimestamp() {
                return w;
            }

            /**
             * Gets number of additions.
             *
             * @return The number of additions for the week.
             */
            public int getNumberOfAdditions() {
                return a;
            }

            /**
             * Gets number of deletions.
             *
             * @return The number of deletions for the week.
             */
            public int getNumberOfDeletions() {
                return d;
            }

            /**
             * Gets number of commits.
             *
             * @return The number of commits for the week.
             */
            public int getNumberOfCommits() {
                return c;
            }

            /**
             * To string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return String.format("Week starting %d - Additions: %d, Deletions: %d, Commits: %d", w, a, d, c);
            }
        }
    }

    /**
     * Get the last year of commit activity data. See
     * https://developer.github.com/v3/repos/statistics/#get-the-last-year-of-commit-activity-data
     *
     * @return the commit activity
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<CommitActivity> getCommitActivity() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("commit_activity"))
                .toIterable(CommitActivity[].class, null);
    }

    /**
     * The type CommitActivity.
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class CommitActivity extends GHObject {
        private List<Integer> days;
        private int total;
        private long week;

        /**
         * Gets days.
         *
         * @return The number of commits for each day of the week. 0 = Sunday, 1 = Monday, etc.
         */
        public List<Integer> getDays() {
            return Collections.unmodifiableList(days);
        }

        /**
         * Gets total.
         *
         * @return The total number of commits for the week.
         */
        public int getTotal() {
            return total;
        }

        /**
         * Gets week.
         *
         * @return The start of the week as a UNIX timestamp.
         */
        public long getWeek() {
            return week;
        }
    }

    /**
     * Get the number of additions and deletions per week. See
     * https://developer.github.com/v3/repos/statistics/#get-the-number-of-additions-and-deletions-per-week
     *
     * @return the code frequency
     * @throws IOException
     *             the io exception
     */
    public List<CodeFrequency> getCodeFrequency() throws IOException {
        try {
            CodeFrequency[] list = root().createRequest()
                    .withUrlPath(getApiTailUrl("code_frequency"))
                    .fetch(CodeFrequency[].class);

            return Arrays.asList(list);
        } catch (MismatchedInputException e) {
            // This sometimes happens when retrieving code frequency statistics
            // for a repository for the first time. It is probably still being
            // generated, so return null.
            return null;
        }
    }

    /**
     * The type CodeFrequency.
     */
    public static class CodeFrequency {

        private final int week;
        private final int additions;
        private final int deletions;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        private CodeFrequency(List<Integer> item) {
            week = item.get(0);
            additions = item.get(1);
            deletions = item.get(2);
        }

        /**
         * Gets week timestamp.
         *
         * @return The start of the week as a UNIX timestamp.
         */
        public int getWeekTimestamp() {
            return week;
        }

        /**
         * Gets additions.
         *
         * @return The number of additions for the week.
         */
        public long getAdditions() {
            return additions;
        }

        /**
         * Gets deletions.
         *
         * @return The number of deletions for the week. NOTE: This will be a NEGATIVE number.
         */
        public long getDeletions() {
            // TODO: Perhaps return Math.abs(deletions),
            // since most developers may not expect a negative number.
            return deletions;
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "Week starting " + getWeekTimestamp() + " has " + getAdditions() + " additions and "
                    + Math.abs(getDeletions()) + " deletions";
        }
    }

    /**
     * Get the weekly commit count for the repository owner and everyone else. See
     * https://developer.github.com/v3/repos/statistics/#get-the-weekly-commit-count-for-the-repository-owner-and-everyone-else
     *
     * @return the participation
     * @throws IOException
     *             the io exception
     */
    public Participation getParticipation() throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("participation")).fetch(Participation.class);
    }

    /**
     * The type Participation.
     */
    public static class Participation extends GHObject {
        private List<Integer> all;
        private List<Integer> owner;

        /**
         * Gets all commits.
         *
         * @return The list of commit counts for everyone combined, for the last 52 weeks.
         */
        public List<Integer> getAllCommits() {
            return Collections.unmodifiableList(all);
        }

        /**
         * Gets owner commits.
         *
         * @return The list of commit counts for the owner, for the last 52 weeks.
         */
        public List<Integer> getOwnerCommits() {
            return Collections.unmodifiableList(owner);
        }
    }

    /**
     * Get the number of commits per hour in each day. See
     * https://developer.github.com/v3/repos/statistics/#get-the-number-of-commits-per-hour-in-each-day
     *
     * @return the punch card
     * @throws IOException
     *             the io exception
     */
    public List<PunchCardItem> getPunchCard() throws IOException {
        PunchCardItem[] list = root().createRequest()
                .withUrlPath(getApiTailUrl("punch_card"))
                .fetch(PunchCardItem[].class);
        return Arrays.asList(list);
    }

    /**
     * The type PunchCardItem.
     */
    public static class PunchCardItem {

        private final int dayOfWeek;
        private final int hourOfDay;
        private final int numberOfCommits;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        private PunchCardItem(List<Integer> item) {
            dayOfWeek = item.get(0);
            hourOfDay = item.get(1);
            numberOfCommits = item.get(2);
        }

        /**
         * Gets day of week.
         *
         * @return The day of the week. 0 = Sunday, 1 = Monday, etc.
         */
        public int getDayOfWeek() {
            return dayOfWeek;
        }

        /**
         * Gets hour of day.
         *
         * @return The hour of the day from 0 to 23.
         */
        public long getHourOfDay() {
            return hourOfDay;
        }

        /**
         * Gets number of commits.
         *
         * @return The number of commits for the day and hour.
         */
        public long getNumberOfCommits() {
            return numberOfCommits;
        }

        /**
         * To string.
         *
         * @return the string
         */
        public String toString() {
            return "Day " + getDayOfWeek() + " Hour " + getHourOfDay() + ": " + getNumberOfCommits() + " commits";
        }
    }

    /**
     * Gets the api tail url.
     *
     * @param tail
     *            the tail
     * @return the api tail url
     */
    String getApiTailUrl(String tail) {
        return repo.getApiTailUrl("stats/" + tail);
    }
}

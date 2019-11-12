package org.kohsuke.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Statistics for a GitHub repository.
 *
 * @author Martin van Zijl
 */
public class GHRepositoryStatistics {

    private final GHRepository repo;
    private final GitHub root;

    private static final int MAX_WAIT_ITERATIONS = 3;
    private static final int WAIT_SLEEP_INTERVAL = 5000;

    public GHRepositoryStatistics(GHRepository repo) {
        this.repo = repo;
        this.root = repo.root;
    }

    /**
     * Get contributors list with additions, deletions, and commit count. See
     * https://developer.github.com/v3/repos/statistics/#get-contributors-list-with-additions-deletions-and-commit-counts
     */
    public PagedIterable<ContributorStats> getContributorStats() throws IOException, InterruptedException {
        return getContributorStats(true);
    }

    /**
     * @param waitTillReady Whether to sleep the thread if necessary until the
     * statistics are ready. This is true by default.
     */
    @Preview
    @Deprecated
    @SuppressWarnings("SleepWhileInLoop")
    @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "JSON API")
    public PagedIterable<ContributorStats> getContributorStats(boolean waitTillReady) throws IOException, InterruptedException {
        PagedIterable<GHRepositoryStatistics.ContributorStats> stats =
                getContributorStatsImpl();

        if (stats == null  &&  waitTillReady) {
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
     * This gets the actual statistics from the server. Returns null if they
     * are still being cached.
     */
    private PagedIterable<ContributorStats> getContributorStatsImpl() throws IOException {
        return root.createRequester().method("GET")
            .asPagedIterable(
                getApiTailUrl("contributors"),
                ContributorStats[].class,
                item -> item.wrapUp(root) );
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
        "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
    public static class ContributorStats extends GHObject {
        /*package almost final*/ private GitHub root;
        private GHUser author;
        private int total;
        private List<Week> weeks;

        @Override
        public URL getHtmlUrl() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public GitHub getRoot() {
            return root;
        }

        /**
         * @return The author described by these statistics.
         */
        public GHUser getAuthor() {
            return author;
        }

        /**
         * @return The total number of commits authored by the contributor.
         */
        public int getTotal() {
            return total;
        }

        /**
         * Convenience method to look up week with particular timestamp.
         *
         * @param timestamp The timestamp to look for.
         * @return The week starting with the given timestamp. Throws an
         * exception if it is not found.
         * @throws NoSuchElementException
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
         * @return The total number of commits authored by the contributor.
         */
        public List<Week> getWeeks() {
            return weeks;
        }

        @Override
        public String toString() {
            return author.getLogin() + " made " + String.valueOf(total)
                    + " contributions over " + String.valueOf(weeks.size())
                    + " weeks";
        }

        @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
            "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
        public static class Week {

            private long w;
            private int a;
            private int d;
            private int c;

            /**
             * @return Start of the week, as a UNIX timestamp.
             */
            public long getWeekTimestamp() {
                return w;
            }

            /**
             * @return The number of additions for the week.
             */
            public int getNumberOfAdditions() {
                return a;
            }

            /**
             * @return The number of deletions for the week.
             */
            public int getNumberOfDeletions() {
                return d;
            }

            /**
             * @return The number of commits for the week.
             */
            public int getNumberOfCommits() {
                return c;
            }

            @Override
            public String toString() {
                return String.format("Week starting %d - Additions: %d, Deletions: %d, Commits: %d", w, a, d, c);
            }
        }

        /*package*/ ContributorStats wrapUp(GitHub root) {
            this.root = root;
            return this;
        }
    }

    /**
     * Get the last year of commit activity data. See
     * https://developer.github.com/v3/repos/statistics/#get-the-last-year-of-commit-activity-data
     */
    public PagedIterable<CommitActivity> getCommitActivity() throws IOException {
        return root.createRequester().method("GET")
            .asPagedIterable(
                getApiTailUrl("commit_activity"),
                CommitActivity[].class,
                item -> item.wrapUp(root) );
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
        "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
    public static class CommitActivity extends GHObject {
        /*package almost final*/ private GitHub root;
        private List<Integer> days;
        private int total;
        private long week;

        /**
         * @return The number of commits for each day of the week. 0 = Sunday, 1
         * = Monday, etc.
         */
        public List<Integer> getDays() {
            return days;
        }

        /**
         * @return The total number of commits for the week.
         */
        public int getTotal() {
            return total;
        }

        /**
         * @return The start of the week as a UNIX timestamp.
         */
        public long getWeek() {
            return week;
        }

        /*package*/ CommitActivity wrapUp(GitHub root) {
            this.root = root;
            return this;
        }

        public GitHub getRoot() {
            return root;
        }

        @Override
        public URL getHtmlUrl() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

        /**
     * Get the number of additions and deletions per week.
     * See https://developer.github.com/v3/repos/statistics/#get-the-number-of-additions-and-deletions-per-week
     */
    public List<CodeFrequency> getCodeFrequency() throws IOException {
        // Map to ArrayLists first, since there are no field names in the
        // returned JSON.
        try {
            InputStream stream = root.createRequester().method("GET").asStream(getApiTailUrl("code_frequency"));

            ObjectMapper mapper = new ObjectMapper();
            TypeReference<ArrayList<ArrayList<Integer> > > typeRef =
                    new TypeReference<ArrayList< ArrayList<Integer> > >() {};
            ArrayList<ArrayList <Integer> > list = mapper.readValue(stream, typeRef);

            // Convert to proper objects.
            ArrayList<CodeFrequency> returnList = new ArrayList<CodeFrequency>();
            for(ArrayList<Integer> item: list)
            {
                CodeFrequency cf = new CodeFrequency(item);
                returnList.add(cf);
            }

            return returnList;
        } catch (MismatchedInputException e) {
            // This sometimes happens when retrieving code frequency statistics
            // for a repository for the first time. It is probably still being
            // generated, so return null.
            return null;
        }
    }

    public static class CodeFrequency {
        private int week;
        private int additions;
        private int deletions;

        private CodeFrequency(ArrayList<Integer> item) {
            week = item.get(0);
            additions = item.get(1);
            deletions = item.get(2);
        }

        /**
         * @return The start of the week as a UNIX timestamp.
         */
        public int getWeekTimestamp() {
            return week;
        }

        /**
         * @return The number of additions for the week.
         */
        public long getAdditions() {
            return additions;
        }

        /**
         * @return The number of deletions for the week.
         * NOTE: This will be a NEGATIVE number.
         */
        public long getDeletions() {
            // TODO: Perhaps return Math.abs(deletions),
            // since most developers may not expect a negative number.
            return deletions;
        }

        @Override
        public String toString() {
            return "Week starting " + getWeekTimestamp() + " has " + getAdditions() +
                    " additions and " + Math.abs(getDeletions()) + " deletions";
        }
    }

    /**
     * Get the weekly commit count for the repository owner and everyone else.
     * See https://developer.github.com/v3/repos/statistics/#get-the-weekly-commit-count-for-the-repository-owner-and-everyone-else
     */
    public Participation getParticipation() throws IOException {
        return root.createRequester().method("GET").to(getApiTailUrl("participation"), Participation.class);
    }

    public static class Participation extends GHObject {
        /*package almost final*/ private GitHub root;
        private List<Integer> all;
        private List<Integer> owner;

        @Override
        public URL getHtmlUrl() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public GitHub getRoot() {
            return root;
        }

        /**
         * @return The list of commit counts for everyone combined, for the
         * last 52 weeks.
         */
        public List<Integer> getAllCommits() {
            return all;
        }

        /**
         * @return The list of commit counts for the owner, for the
         * last 52 weeks.
         */
        public List<Integer> getOwnerCommits() {
            return owner;
        }

        /*package*/ Participation wrapUp(GitHub root) {
        this.root = root;
        return this;
        }
    }

    /**
     * Get the number of commits per hour in each day.
     * See https://developer.github.com/v3/repos/statistics/#get-the-number-of-commits-per-hour-in-each-day
     */
    public List<PunchCardItem> getPunchCard() throws IOException {
        // Map to ArrayLists first, since there are no field names in the
        // returned JSON.
        InputStream stream = root.createRequester().method("GET").asStream(getApiTailUrl("punch_card"));

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<ArrayList<ArrayList<Integer> > > typeRef =
                new TypeReference<ArrayList< ArrayList<Integer> > >() {};
        ArrayList<ArrayList <Integer> > list = mapper.readValue(stream, typeRef);

        // Convert to proper objects.
        ArrayList<PunchCardItem> returnList = new ArrayList<PunchCardItem>();
        for(ArrayList<Integer> item: list) {
            PunchCardItem pci = new PunchCardItem(item);
            returnList.add(pci);
        }

        return returnList;
    }

    public static class PunchCardItem {
        private int dayOfWeek;
        private int hourOfDay;
        private int numberOfCommits;

        private PunchCardItem(ArrayList<Integer> item) {
            dayOfWeek = item.get(0);
            hourOfDay = item.get(1);
            numberOfCommits = item.get(2);
        }

        /**
         * @return The day of the week.
         * 0 = Sunday, 1 = Monday, etc.
         */
        public int getDayOfWeek() {
            return dayOfWeek;
        }

        /**
         * @return The hour of the day from 0 to 23.
         */
        public long getHourOfDay() {
            return hourOfDay;
        }

        /**
         * @return The number of commits for the day and hour.
         */
        public long getNumberOfCommits() {
            return numberOfCommits;
        }

        public String toString() {
            return "Day " + getDayOfWeek() + " Hour " + getHourOfDay() + ": " +
                    getNumberOfCommits() + " commits";
        }
    }

    String getApiTailUrl(String tail) {
        return repo.getApiTailUrl("stats/" + tail);
    }
}

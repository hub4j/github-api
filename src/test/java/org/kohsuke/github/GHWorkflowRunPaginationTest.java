package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class GHWorkflowRunPaginationTest extends AbstractGitHubWireMockTest {
    private static final String REPO_NAME = "hub4j/github-api";
    private static final String SINGLE_RUN_REPO_NAME = "hub4j-test-org/GHWorkflowRunTest";
    private GHRepository repo;

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(REPO_NAME);
    }

    @Test
    public void testBasicPagination() throws Exception {
        Paginator<GHWorkflowRun> paginator = repo.getWorkflow("maven-build.yml")
                .listRuns()
                .withStartPage(6)
                .withPageSize(6)
                .paginator();

        // total pages
        assertThat(paginator.totalPages(), greaterThan(0));

        // current page
        assertThat(paginator.currentPage(), equalTo(6));

        // last
        assertThat(paginator.last().getRunNumber(), equalTo(3084L));
        assertThat(paginator.hasNext(), equalTo(false));
        assertThat(paginator.hasPrevious(), equalTo(true));

        // first, and moving around
        GHWorkflowRun first = paginator.first();
        assertThat(first, notNullValue());
        assertThat(paginator.hasNext(), equalTo(true));
        assertThat(paginator.hasPrevious(), equalTo(true));
        assertThat(paginator.previous().getRunNumber(), equalTo(first.getRunNumber()));
        assertThat(paginator.hasPrevious(), equalTo(false));
        assertThat(paginator.hasNext(), equalTo(true));
        assertThat(paginator.nextPage().size(), equalTo(6));
        assertThat(paginator.previousPage().size(), equalTo(6));
        assertThat(paginator.hasPrevious(), equalTo(false));
        assertThat(paginator.hasNext(), equalTo(true));

        // starting at 0 index, check previousPage size from middle of the page
        paginator.next();
        paginator.next();
        assertThat(paginator.previousPage().size(), equalTo(2));

        // starting at 0 index, check nextPage size from middle of the page
        paginator.next();
        paginator.next();
        assertThat(paginator.nextPage().size(), equalTo(4));

        // next page
        int pageNumber = paginator.currentPage();
        for (int i = 0; i < 4; i++) {
            List<GHWorkflowRun> page = paginator.nextPage();
            assertThat(page.size(), equalTo(6));
            assertThat(paginator.currentPage(), equalTo(++pageNumber));
        }

        // previous page
        pageNumber = paginator.currentPage();
        for (int i = 0; i < 4; i++) {
            List<GHWorkflowRun> page = paginator.previousPage();
            assertThat(page.size(), equalTo(6));
            assertThat(paginator.currentPage(), equalTo(pageNumber--));
        }

        // next and previous over multiple pages
        long[] ascending = new long[14];
        long[] descending = new long[14];
        for (int i = 0; i < 14; i++) {
            ascending[i] = paginator.next().getRunNumber();
        }
        for (int i = 13; i >= 0; i--) {
            descending[i] = paginator.previous().getRunNumber();
        }
        assertThat(ascending, equalTo(descending));

        // jump to page
        assertThat(paginator.jumpToPage(4).currentPage(), equalTo(4));
        assertThat(paginator.jumpToPage(8).currentPage(), equalTo(8));
        assertThat(paginator.jumpToPage(6).currentPage(), equalTo(6));

        // first page list vs jump to page 1
        assertThat(getRunNumbers(paginator.firstPageList()),
                equalTo(getRunNumbers(paginator.jumpToPage(1).nextPage())));

        // last page list vs jump to page number totalPages
        assertThat(getRunNumbers(paginator.lastPageList()),
                equalTo(getRunNumbers(paginator.jumpToPage(paginator.totalPages()).nextPage())));
    }

    @Test
    public void testRepoWithSingleRun() throws Exception {
        Paginator<GHWorkflowRun> paginator = gitHub.getRepository(SINGLE_RUN_REPO_NAME)
                .queryWorkflowRuns()
                .list()
                .withPageSize(6)
                .paginator();
        // total pages
        assertThat(paginator.totalPages(), equalTo(1));

        // current page
        assertThat(paginator.currentPage(), equalTo(1));

        // last
        assertThat(paginator.last().getRunNumber(), equalTo(78L));
        assertThat(paginator.hasNext(), equalTo(false));
        assertThat(paginator.hasPrevious(), equalTo(true));

        // first
        assertThat(paginator.first().getRunNumber(), equalTo(78L));
        assertThat(paginator.hasNext(), equalTo(false));
        assertThat(paginator.hasPrevious(), equalTo(true));

        // pages
        List<Long> list = Collections.singletonList(78L);
        assertThat(getRunNumbers(paginator.lastPageList()), equalTo(list));
        assertThat(getRunNumbers(paginator.firstPageList()), equalTo(list));
        assertThat(getRunNumbers(paginator.jumpToPage(1).nextPage()), equalTo(list));

        // previous
        assertThat(paginator.hasPrevious(), equalTo(true));
        assertThat(paginator.hasNext(), equalTo(false));
        assertThat(paginator.previous().getRunNumber(), equalTo(78L));

        // next
        assertThat(paginator.hasPrevious(), equalTo(false));
        assertThat(paginator.hasNext(), equalTo(true));
        assertThat(paginator.next().getRunNumber(), equalTo(78L));

        // previous page
        assertThat(getRunNumbers(paginator.previousPage()), equalTo(list));
        assertThat(paginator.hasPrevious(), equalTo(false));
        assertThat(paginator.hasNext(), equalTo(true));

        // next page
        assertThat(getRunNumbers(paginator.nextPage()), equalTo(list));
        assertThat(paginator.hasPrevious(), equalTo(true));
        assertThat(paginator.hasNext(), equalTo(false));
    }

    private static List<Long> getRunNumbers(List<GHWorkflowRun> runs) {
        return runs.stream().map(GHWorkflowRun::getRunNumber).collect(Collectors.toList());
    }
}

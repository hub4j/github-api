package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class GHWorkflowRunPaginationTest extends AbstractGitHubWireMockTest {
    private static final String REPO_NAME = "hub4j/github-api";
    private static final String SINGLE_RUN_REPO_NAME = "hub4j-test-org/GHWorkflowRunTest";
    private static final String NO_RUNS_REPO_NAME = "hub4j-test-org/GHWorkflowTest";
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
        assertThat(paginator.last().getRunNumber(), equalTo(3089L));
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
    public void testPageWithSizeEqualTo1() throws Exception {
        Paginator<GHWorkflowRun> paginator1 = repo.getWorkflow("maven-build.yml")
                .listRuns()
                .withPageSize(1)
                .paginator();
        Paginator<GHWorkflowRun> paginator2 = repo.getWorkflow("maven-build.yml")
                .listRuns()
                .withPageSize(1)
                .paginator();

        List<GHWorkflowRun> page = paginator1.nextPage();
        assertThat(page.size(), equalTo(1));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator2.next().getRunNumber()));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator1.previous().getRunNumber()));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator2.previousPage().get(0).getRunNumber()));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator1.firstPageList().get(0).getRunNumber()));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator1.first().getRunNumber()));

        page = paginator1.lastPageList();
        assertThat(page.size(), equalTo(1));
        assertThat(page.get(0).getRunNumber(), equalTo(paginator2.last().getRunNumber()));

        // jump to page
        assertThat(paginator1.jumpToPage(4).currentPage(), equalTo(4));
        assertThat(paginator1.jumpToPage(8).currentPage(), equalTo(8));
        assertThat(paginator1.jumpToPage(6).currentPage(), equalTo(6));

        // next and previous over multiple pages
        long[] ascending = new long[14];
        long[] descending = new long[14];
        for (int i = 0; i < 14; i++) {
            ascending[i] = paginator1.next().getRunNumber();
        }
        for (int i = 13; i >= 0; i--) {
            descending[i] = paginator1.previous().getRunNumber();
        }
        assertThat(ascending, equalTo(descending));

        // first page list vs jump to page 1
        assertThat(getRunNumbers(paginator1.firstPageList()),
                equalTo(getRunNumbers(paginator1.jumpToPage(1).nextPage())));

        // last page list vs jump to page number totalPages
        assertThat(getRunNumbers(paginator1.lastPageList()),
                equalTo(getRunNumbers(paginator1.jumpToPage(paginator1.totalPages()).nextPage())));
    }

    @Test
    public void testNext() throws Exception {
        assertThat(3585L, equalTo(getNewPaginator().next().getRunNumber()));
    }

    @Test
    public void testHasNextThenNext() throws Exception {
        Paginator<GHWorkflowRun> paginator = getNewPaginator();
        assertThat(paginator.hasNext(), equalTo(true));
        assertThat(3585L, equalTo(paginator.next().getRunNumber()));
    }

    @Test
    public void testPrevious() throws Exception {
        assertThat(3586L, equalTo(getNewPaginator().previous().getRunNumber()));
    }

    @Test
    public void testFirst() throws Exception {
        assertThat(3590L, equalTo(getNewPaginator().first().getRunNumber()));
    }

    @Test
    public void testLast() throws Exception {
        assertThat(3089L, equalTo(getNewPaginator().last().getRunNumber()));
    }

    @Test
    public void testHasNext() throws Exception {
        assertThat(true, equalTo(getNewPaginator().hasNext()));
    }

    @Test
    public void testHasPrevious() throws Exception {
        assertThat(true, equalTo(getNewPaginator().hasPrevious()));
    }

    @Test
    public void testNextPage() throws Exception {
        assertThat(Arrays.asList(3585L, 3584L, 3583L, 3582L, 3581L),
                equalTo(getRunNumbers(getNewPaginator().nextPage())));
    }

    @Test
    public void testPreviousPage() throws Exception {
        assertThat(Arrays.asList(3590L, 3589L, 3588L, 3587L, 3586L),
                equalTo(getRunNumbers(getNewPaginator().previousPage())));
    }

    @Test
    public void testFirstPage() throws Exception {
        assertThat(Arrays.asList(3590L, 3589L, 3588L, 3587L, 3586L),
                equalTo(getRunNumbers(getNewPaginator().firstPageList())));
    }

    @Test
    public void testLastPage() throws Exception {
        assertThat(List.of(3089L), equalTo(getRunNumbers(getNewPaginator().lastPageList())));
    }

    @Test
    public void testJumpToPage() throws Exception {
        assertThat(3580L, equalTo(getNewPaginator().jumpToPage(3).next().getRunNumber()));
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

    @Test
    public void testRepoWithNoRuns() throws Exception {
        Paginator<GHWorkflowRun> paginator = getPaginatorWithNoRuns();
        assertThat(paginator.hasNext(), equalTo(false));
        assertThat(paginator.hasPrevious(), equalTo(false));
        assertThat(getPaginatorWithNoRuns().hasPrevious(), equalTo(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_last_throwsException() throws Exception {
        getPaginatorWithNoRuns().last();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_first_throwsException() throws Exception {
        getPaginatorWithNoRuns().first();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_next_throwsException() throws Exception {
        getPaginatorWithNoRuns().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_previous_throwsException() throws Exception {
        getPaginatorWithNoRuns().previous();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_nextPage_emptyList() throws Exception {
        getPaginatorWithNoRuns().nextPage();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepoWithNoRuns_previousPage_emptyList() throws Exception {
        getPaginatorWithNoRuns().previousPage();
    }

    @Test
    public void testRepoWithNoRuns_firstPage_emptyList() throws Exception {
        assertThat(getPaginatorWithNoRuns().firstPageList(), empty());
    }

    @Test
    public void testRepoWithNoRuns_lastPage_emptyList() throws Exception {
        assertThat(getPaginatorWithNoRuns().lastPageList(), empty());
    }

    @Test
    public void testRepoWithNoRuns_totalPages_zero() throws Exception {
        assertThat(getPaginatorWithNoRuns().totalPages(), equalTo(0));
    }

    @NotNull
    private Paginator<GHWorkflowRun> getPaginatorWithNoRuns() throws IOException {
        return gitHub.getRepository(NO_RUNS_REPO_NAME).queryWorkflowRuns().list().paginator();
    }

    private static List<Long> getRunNumbers(List<GHWorkflowRun> runs) {
        return runs.stream().map(GHWorkflowRun::getRunNumber).collect(Collectors.toList());
    }

    private Paginator<GHWorkflowRun> getNewPaginator() throws IOException {
        return repo.getWorkflow("maven-build.yml").listRuns().withPageSize(5).withStartPage(2).paginator();
    }
}

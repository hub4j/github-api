package org.kohsuke.github;

import org.junit.Test;

import java.io.*;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

/**
 * The Class PaginatedEndpointTest.
 *
 * @author Liam Newman
 */
public class PaginatedEndpointTest extends AbstractGitHubWireMockTest {

    /**
     * Test
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void peekNullShouldThrow() throws IOException {
        var page = new GitHubPageArrayAdapter<>(new Object[]{});
        var pageType = (Class<GitHubPageArrayAdapter<Object>>) page.getClass();

        // calling next
        var emptyPages = new PaginatedEndpointPages<>(pageType, page);
        var emptyPageItems = emptyPages.next().getItems();
        assertThat(emptyPageItems.length, equalTo(0));
        assertThat(emptyPages.hasNext(), equalTo(false));
        // Calling next when hasNext() is false, throws.
        assertThrows(NoSuchElementException.class, () -> emptyPages.next());

        // Calling items.next() on an empty result should throw.
        var items = new PaginatedEndpointItems<>(new PaginatedEndpointPages<>(pageType, page));
        assertThat(items.peek(), equalTo(null));
        assertThat(items.hasNext(), equalTo(false));
        assertThrows(NoSuchElementException.class, () -> items.next());
    }

    /**
     * Test
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeprecatedNextPage() throws IOException {
        var page = new GitHubPageArrayAdapter<>(new Object[]{ 1, 2, 3 });
        var pageType = (Class<GitHubPageArrayAdapter<Object>>) page.getClass();

        var items = new PaginatedEndpointItems<>(new PaginatedEndpointPages<>(pageType, page));
        assertThat(items.next(), equalTo(1));
        // Current Page on a partially read page should return full page
        var itemsPage = items.getCurrentPage().getItems();
        assertThat(itemsPage.length, equalTo(3));

        var partialPage = items.nextPage();
        // Next Page on a partially read page should return
        assertThat(partialPage.size(), equalTo(2));
        assertThrows(NoSuchElementException.class, () -> items.next());
        assertThrows(NoSuchElementException.class, () -> items.nextPage());
    }

    /**
     * Test
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testIterators() throws IOException {
        var page = new GitHubPageArrayAdapter<>(new Object[]{ 1, 2, 3 });

        var endpoint = PaginatedEndpoint.fromSinglePage(page, Object.class);

        var iterator = endpoint.iterator();
        assertThat(iterator.next(), equalTo(1));
        assertThat(iterator.next(), equalTo(2));
        assertThat(iterator.next(), equalTo(3));
        assertThat(iterator.hasNext(), equalTo(false));

        var pagedIterator = new PagedIterator<>(endpoint.items());
        assertThat(pagedIterator.next(), equalTo(1));
        var nextPage = pagedIterator.nextPage();
        assertThat(nextPage.size(), equalTo(2));
        assertThat(iterator.hasNext(), equalTo(false));
    }

    /**
     * Test
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testNextPageError() throws IOException {
        var page = new GitHubPageArrayAdapter<>(new Object[]{}) {
            @Override
            public Object[] getItems() {
                throw new GHException("outer", new Exception("inner"));
            }
        };

        var pageIo = new GitHubPageArrayAdapter<>(new Object[]{}) {
            @Override
            public Object[] getItems() {
                throw new GHException("outer", new IOException("inner"));
            }
        };

        final var pages = PaginatedEndpoint.fromSinglePage(page, Object.class).pages();

        var e = assertThrows(GHException.class, () -> pages.finalResponse());
        assertThat(e.getMessage(), equalTo("Final response is not available until after iterator is done."));

        var ex = assertThrows(GHException.class, () -> PaginatedEndpoint.toList(pages, Object.class));
        assertThat(ex.getMessage(), equalTo("outer"));

        final var pagesIo = PaginatedEndpoint.fromSinglePage(pageIo, Object.class).pages();
        var exIo = assertThrows(IOException.class, () -> PaginatedEndpoint.toList(pagesIo, Object.class));
        assertThat(exIo.getMessage(), equalTo("inner"));
    }

}

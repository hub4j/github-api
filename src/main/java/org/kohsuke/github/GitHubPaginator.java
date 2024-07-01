package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

/**
 * May be used for any item that has pagination information. Iterates over paginated {@link T} objects (not the items
 * inside the page). Equivalent to {@link GitHubPageIterator} but with increased functionality, supporting bidirectional
 * movement and jumping to first, last or any specific page.
 * <p>
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 * <p>
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @author Anuj Hydrabadi
 * @param <T>
 *            type of each page (not the items in the page).
 */
public class GitHubPaginator<T> implements NavigablePageIterator<T> {
    private final GitHubClient client;
    private final Class<T> type;

    /** Current page number. */
    private int currentPage;
    /** Total pages. */
    private int finalPage;
    /** The latest request that was sent. */
    private GitHubRequest currentRequest;
    /** Whether there is a previous page. Refreshed every time a request is made. */
    private boolean hasPrevious;
    /** Whether there is a next page. Refreshed every time a request is made. */
    private boolean hasNext;
    /** Whether at least one API call is made. Starts as false when object is created, once set to true, stays true. */
    private boolean firstCallMade;
    private T next;
    private GitHubPaginator(GitHubClient client, Class<T> type, GitHubRequest request, int startPage) {
        if (!"GET".equals(request.method())) {
            throw new IllegalStateException("Request method \"GET\" is required for page iterator.");
        }

        this.client = client;
        this.type = type;
        this.currentRequest = request;

        this.currentPage = startPage;
        this.firstCallMade = false;
    }

    /**
     * Loads paginated resources.
     *
     * @param client
     *            the {@link GitHubClient} from which to request responses
     * @param type
     *            type of each page (not the items in the page).
     * @param request
     *            the request
     * @param pageSize
     *            the page size
     * @param startPage
     *            the page to start from
     * @return the paginator
     * @param <T>
     *            type of each page (not the items in the page).
     */
    static <T> GitHubPaginator<T> create(GitHubClient client,
            Class<T> type,
            GitHubRequest request,
            int pageSize,
            int startPage) {

        if (pageSize > 0) {
            GitHubRequest.Builder<?> builder = request.toBuilder().with("per_page", pageSize);
            request = builder.build();
        }

        if (startPage > 0) {
            GitHubRequest.Builder<?> builder = request.toBuilder().set("page", startPage);
            request = builder.build();
        }

        return new GitHubPaginator<>(client, type, request, startPage);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        initialise();
        return hasNext;
    }

    /** {@inheritDoc} */
    @Override
    public T next() {
        initialise();
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        if (next != null)
            return next;

        currentRequest = currentRequest.toBuilder().set("page", String.valueOf(++currentPage)).build();
        return makeRequest(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPrevious() {
        initialise();
        return hasPrevious;
    }

    /** {@inheritDoc} */
    @Override
    public T previous() {
        initialise();
        if (!hasPrevious) {
            throw new NoSuchElementException();
        }
        currentRequest = currentRequest.toBuilder().set("page", String.valueOf(--currentPage)).build();
        return makeRequest(false);
    }

    /** {@inheritDoc} */
    @Override
    public T first() {
        initialise();
        currentPage = 1;
        currentRequest = currentRequest.toBuilder().set("page", String.valueOf(currentPage)).build();
        return makeRequest(false);
    }

    /** {@inheritDoc} */
    @Override
    public T last() {
        initialise();
        currentPage = finalPage;
        currentRequest = currentRequest.toBuilder().set("page", String.valueOf(currentPage)).build();
        return makeRequest(false);
    }

    /** {@inheritDoc} */
    @Override
    public int totalCount() {
        initialise();
        return finalPage;
    }

    private void initialise() {
        if (!firstCallMade) {
            makeRequest(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int currentPage() {
        return currentPage;
    }

    /** {@inheritDoc} */
    @Override
    public T jumpToPage(int page) {
        initialise();
        if (page < 1 || page > finalPage) {
            throw new NoSuchElementException();
        }
        currentPage = page;
        currentRequest = currentRequest.toBuilder().set("page", String.valueOf(currentPage)).build();
        return makeRequest(false);
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        if (!firstCallMade) {
            throw new GHException("Cannot refresh before the first call has been made!");
        }
        if (client.isOffline()) {
            return; // cannot populate, will have to live with what we have
        }
        makeRequest(false);
    }

    /**
     * Make the request specified in {@link #currentRequest}, and update all other state variables of the class.
     *
     * @return the response
     */
    private T makeRequest(boolean firstCall) {
        URL url = currentRequest.url();
        try {
            GitHubResponse<T> response = client.sendRequest(currentRequest,
                    (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, type));
            assert response.body() != null;

            updateState(currentRequest, response, firstCall);
            firstCallMade = true;
            return response.body();
        } catch (IOException e) {
            // Iterators do not throw IOExceptions, so we wrap any IOException
            // in a runtime GHException to bubble out if needed.
            throw new GHException("Failed to retrieve " + url, e);
        }
    }

    /**
     * Called after every request is made. Updates the state of the class comprising three fields: ({@link #hasNext},
     * {@link #hasPrevious}, {@link #finalPage}), based on the "Link" header.
     *
     * @param currentRequest
     *            the request just made.
     * @param currentResponse
     *            the response just received.
     */
    private void updateState(GitHubRequest currentRequest, GitHubResponse<T> currentResponse, boolean firstCall) {
        hasPrevious = false;
        hasNext = false;
        finalPage = getPageFromUrl(currentRequest.url());

        String link = currentResponse.header("Link");
        if (link != null) {
            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    hasNext = true;
                } else if (token.endsWith("rel=\"prev\"")) {
                    // found the previous page. This should look something like
                    // <https://api.github.com/repos?page=1&per_page=100>; rel="prev"
                    hasPrevious = true;
                } else if (token.endsWith("rel=\"last\"")) {
                    // found the last page. This should look something like
                    // <https://api.github.com/repos?page=42&per_page=100>; rel="last"
                    int idx = token.indexOf('>');
                    String url = token.substring(1, idx);
                    try {
                        finalPage = getPageFromUrl(new URI(url).toURL());
                    } catch (URISyntaxException | MalformedURLException exception) {
                        throw new GHException(String.format("Unable to extract last page from url %s", url), exception);
                    }
                }
            }
        }
        if (firstCall) {
            hasNext = true;
            next = currentResponse.body();
        } else {
            next = null;
        }

    }

    /**
     * @param url
     *            of type "https://api.github.com/repos?page=42&per_page=100"
     * @return the value of the "page" param from the url as int if present, else 1.
     */
    private static int getPageFromUrl(URL url) {
        String query = url.getQuery();
        String[] queryParams = query.split("&");
        for (String param : queryParams) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals("page")) {
                // Return the "page" parameter as an integer
                return Integer.parseInt(keyValue[1]);
            }
        }
        return 1;
    }
}

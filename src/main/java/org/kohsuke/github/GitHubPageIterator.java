package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 * May be used for any item that has pagination information. Iterates over paginated {@link T} objects (not the items
 * inside the page). Also exposes {@link #finalResponse()} to allow getting a full {@link GitHubResponse<T>} after
 * iterating completes.
 *
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 *
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @param <T>
 *            type of each page (not the items in the page).
 */
class GitHubPageIterator<T> implements Iterator<T> {

    private final GitHubClient client;
    private final Class<T> type;

    /**
     * The page that will be returned when {@link #next()} is called.
     *
     * <p>
     * Will be {@code null} after {@link #next()} is called.
     * </p>
     * <p>
     * Will not be {@code null} after {@link #fetch()} is called if a new page was fetched.
     * </p>
     */
    private T next;

    /**
     * The request that will be sent when to get a new response page if {@link #next} is {@code null}. Will be
     * {@code null} when there are no more pages to fetch.
     */
    private GitHubRequest nextRequest;

    /**
     * The name of the field which contains a list of items
     */
    private String nestedFieldKey;

    /**
     * When done iterating over pages, it is on rare occasions useful to be able to get information from the final
     * response that was retrieved.
     */
    private GitHubResponse<T> finalResponse = null;

    private GitHubPageIterator(GitHubClient client, Class<T> type, GitHubRequest request, String nestedFieldKey) {
        if (!"GET".equals(request.method())) {
            throw new IllegalStateException("Request method \"GET\" is required for page iterator.");
        }

        this.client = client;
        this.type = type;
        this.nextRequest = request;
        this.nestedFieldKey = nestedFieldKey;
    }

    /**
     * Loads paginated resources.
     *
     * @param client
     *            the {@link GitHubClient} from which to request responses
     * @param type
     *            type of each page (not the items in the page).
     * @param <T>
     *            type of each page (not the items in the page).
     * @return iterator
     */
    static <T> GitHubPageIterator<T> create(GitHubClient client, Class<T> type, GitHubRequest request, int pageSize) {
        return create(client, type, request, pageSize, "");
    }

    /**
     * Loads paginated resources.
     *
     * @param client
     *            the {@link GitHubClient} from which to request responses
     * @param type
     *            type of each page (not the items in the page).
     * @param <T>
     *            type of each page (not the items in the page).
     * @param nestedFieldKey
     *            the name of the field containing the items in the response
     *
     * @return iterator
     */
    static <T> GitHubPageIterator<T> create(GitHubClient client,
            Class<T> type,
            GitHubRequest request,
            int pageSize,
            String nestedFieldKey) {

        try {
            if (pageSize > 0) {
                GitHubRequest.Builder<?> builder = request.toBuilder().with("per_page", pageSize);
                request = builder.build();
            }

            return new GitHubPageIterator<>(client, type, request, nestedFieldKey);
        } catch (MalformedURLException e) {
            throw new GHException("Unable to build GitHub API URL", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        fetch();
        return next != null;
    }

    /**
     * Gets the next page.
     *
     * @return the next page.
     */
    @Nonnull
    public T next() {
        fetch();
        T result = next;
        if (result == null)
            throw new NoSuchElementException();
        // If this is the last page, keep the response
        next = null;
        return result;
    }

    /**
     * On rare occasions the final response from iterating is needed.
     *
     * @return the final response of the iterator.
     */
    public GitHubResponse<T> finalResponse() {
        if (hasNext()) {
            throw new GHException("Final response is not available until after iterator is done.");
        }
        return finalResponse;
    }

    /**
     * Fetch is called at the start of {@link #hasNext()} or {@link #next()} to fetch another page of data if it is
     * needed.
     * <p>
     * If {@link #next} is not {@code null}, no further action is needed. If {@link #next} is {@code null} and
     * {@link #nextRequest} is {@code null}, there are no more pages to fetch.
     * </p>
     * <p>
     * Otherwise, a new response page is fetched using {@link #nextRequest}. The response is then checked to see if
     * there is a page after it and {@link #nextRequest} is updated to point to it. If there are no pages available
     * after the current response, {@link #nextRequest} is set to {@code null}.
     * </p>
     */
    private void fetch() {
        if (next != null)
            return; // already fetched
        if (nextRequest == null)
            return; // no more data to fetch

        URL url = nextRequest.url();
        try {
            GitHubResponse<T> nextResponse = client.sendRequest(nextRequest,
                    (responseInfo) -> GitHubResponse.parseBody(responseInfo, type, nestedFieldKey));
            assert nextResponse.body() != null;
            next = nextResponse.body();
            nextRequest = findNextURL(nextResponse);
            if (nextRequest == null) {
                finalResponse = nextResponse;
            }
        } catch (IOException e) {
            // Iterators do not throw IOExceptions, so we wrap any IOException
            // in a runtime GHException to bubble out if needed.
            throw new GHException("Failed to retrieve " + url, e);
        }
    }

    /**
     * Locate the next page from the pagination "Link" tag.
     */
    private GitHubRequest findNextURL(GitHubResponse<T> nextResponse) throws MalformedURLException {
        GitHubRequest result = null;
        String link = nextResponse.headerField("Link");
        if (link != null) {
            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    int idx = token.indexOf('>');
                    result = nextResponse.request().toBuilder().setRawUrlPath(token.substring(1, idx)).build();
                    break;
                }
            }
        }
        return result;
    }

}

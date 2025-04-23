package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * May be used for any item that has pagination information. Iterates over paginated {@code P} objects (not the items
 * inside the page). Also exposes {@link #finalResponse()} to allow getting a full {@link GitHubResponse}{@code
 *
<P>
 * } after iterating completes.
 * <p>
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 * <p>
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @author Liam Newman
 * @param <P>
 *            type of each page (not the items in the page).
 */
class GitHubEndpointPageIterator<P extends GitHubPage<Item>, Item> extends GitHubPageIterator<P, Item> {

    /**
     * When done iterating over pages, it is on rare occasions useful to be able to get information from the final
     * response that was retrieved.
     */
    private GitHubResponse<P> finalResponse = null;

    protected final GitHubClient client;

    /**
     * The request that will be sent when to get a new response page if {@link #next} is {@code null}. Will be
     * {@code null} when there are no more pages to fetch.
     */
    protected GitHubRequest nextRequest;

    GitHubEndpointPageIterator(GitHubClient client,
            Class<P> pageType,
            GitHubRequest request,
            int pageSize,
            Consumer<Item> itemInitializer) {
        super(pageType, itemInitializer);

        if (pageSize > 0) {
            GitHubRequest.Builder<?> builder = request.toBuilder().with("per_page", pageSize);
            request = builder.build();
        }

        if (request != null && !"GET".equals(request.method())) {
            throw new IllegalArgumentException("Request method \"GET\" is required for page iterator.");
        }

        this.client = client;
        this.nextRequest = request;
    }

    /**
     * On rare occasions the final response from iterating is needed.
     *
     * @return the final response of the iterator.
     */
    public GitHubResponse<P> finalResponse() {
        if (hasNext()) {
            throw new GHException("Final response is not available until after iterator is done.");
        }
        return finalResponse;
    }

    /**
     * Locate the next page from the pagination "Link" tag.
     */
    private void updateNextRequest(GitHubResponse<P> nextResponse) {
        GitHubRequest result = null;
        String link = nextResponse.header("Link");
        if (link != null) {
            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    int idx = token.indexOf('>');
                    result = nextRequest.toBuilder().setRawUrlPath(token.substring(1, idx)).build();
                    break;
                }
            }
        }
        nextRequest = result;
        if (nextRequest == null) {
            // If this is the last page, keep the response
            finalResponse = nextResponse;
        }
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
    @Override
    protected P fetchNext() {
        if (next != null || nextRequest == null)
            return null; // already fetched or no more data to fetch

        P result;

        URL url = nextRequest.url();
        try {
            GitHubResponse<P> nextResponse = sendNextRequest();
            assert nextResponse.body() != null;
            result = nextResponse.body();
            updateNextRequest(nextResponse);
        } catch (IOException e) {
            // Iterators do not throw IOExceptions, so we wrap any IOException
            // in a runtime GHException to bubble out if needed.
            throw new GHException("Failed to retrieve " + url, e);
        }
        return result;
    }

    @NotNull protected GitHubResponse<P> sendNextRequest() throws IOException {
        return client.sendRequest(nextRequest,
                (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, pageType));
    }

}

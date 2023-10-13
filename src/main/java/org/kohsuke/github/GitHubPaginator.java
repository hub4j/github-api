package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

public class GitHubPaginator<T> implements NavigableIterator<T> {
    private final GitHubClient client;
    private final Class<T> type;

    private int currentPage;
    private int finalPage;
    private GitHubRequest currentRequest;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean firstCallMade;
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
            GitHubRequest.Builder<?> builder = request.toBuilder().with("page", startPage);
            request = builder.build();
        }

        return new GitHubPaginator<>(client, type, request, startPage);
    }

    @Override
    public boolean hasNext() {
        if (!firstCallMade) {
            makeRequest();
        }
        return hasNext;
    }

    public int totalPages() {
        return finalPage;
    }

    @Override
    public T next() {
        if (!firstCallMade) {
            makeRequest();
        }
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        currentRequest = currentRequest.toBuilder().with("page", String.valueOf(++currentPage)).build();
        return makeRequest();
    }

    private T makeRequest() {
        URL url = currentRequest.url();
        try {
            GitHubResponse<T> response = client.sendRequest(currentRequest,
                    (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, type));
            assert response.body() != null;

            updateRequests(currentRequest, response);
            firstCallMade = true;
            return response.body();
        } catch (IOException e) {
            // Iterators do not throw IOExceptions, so we wrap any IOException
            // in a runtime GHException to bubble out if needed.
            throw new GHException("Failed to retrieve " + url, e);
        }
    }

    @Override
    public boolean hasPrevious() {
        if (!firstCallMade) {
            makeRequest();
        }
        return hasPrevious;
    }

    @Override
    public T previous() {
        if (!firstCallMade) {
            makeRequest();
        }
        if (!hasPrevious) {
            throw new NoSuchElementException();
        }
        currentRequest = currentRequest.toBuilder().with("page", String.valueOf(--currentPage)).build();
        return makeRequest();
    }

    @Override
    public T first() {
        if (!firstCallMade) {
            makeRequest();
        }
        currentPage = 1;
        currentRequest = currentRequest.toBuilder().with("page", String.valueOf(currentPage)).build();
        return makeRequest();
    }

    @Override
    public T last() {
        if (!firstCallMade) {
            makeRequest();
        }
        currentPage = finalPage;
        currentRequest = currentRequest.toBuilder().with("page", String.valueOf(currentPage)).build();
        return makeRequest();
    }

    @Override
    public int totalCount() {
        if (!firstCallMade) {
            makeRequest();
        }
        return finalPage;
    }

    @Override
    public int currentPage() {
        return currentPage;
    }

    private void updateRequests(GitHubRequest currentRequest, GitHubResponse<T> currentResponse) {
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
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="prev"
                    hasPrevious = true;
                } else if (token.endsWith("rel=\"last\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="last"
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

    }

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

package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * May be used for any item that has pagination information.
 *
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 *
 * @param <T>
 *            type of each page (not the items in the page).
 */
class GitHubPageResponseIterator<T> implements Iterator<GitHubResponse<T>> {

    private final GitHubClient client;
    private final Class<T> type;
    private GitHubRequest nextRequest;
    private GitHubResponse<T> next;

    GitHubPageResponseIterator(GitHubClient client, Class<T> type, GitHubRequest request) {
        this.client = client;
        this.type = type;
        this.nextRequest = request;
    }

    public boolean hasNext() {
        fetch();
        return next != null;
    }

    public GitHubResponse<T> next() {
        fetch();
        GitHubResponse<T> r = next;
        if (r == null)
            throw new NoSuchElementException();
        next = null;
        return r;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void fetch() {
        if (next != null)
            return; // already fetched
        if (nextRequest == null)
            return; // no more data to fetch

        URL url = nextRequest.url();
        try {
            next = client.sendRequest(nextRequest, (responseInfo) -> GitHubClient.parseBody(responseInfo, type));
            assert next.body() != null;
            nextRequest = findNextURL();
        } catch (IOException e) {
            throw new GHException("Failed to retrieve " + url, e);
        }
    }

    /**
     * Locate the next page from the pagination "Link" tag.
     */
    private GitHubRequest findNextURL() throws MalformedURLException {
        GitHubRequest result = null;
        String link = next.headerField("Link");
        if (link != null) {
            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    int idx = token.indexOf('>');
                    result = next.request().builder().build(client, new URL(token.substring(1, idx)));
                    break;
                }
            }
        }
        return result;
    }
}

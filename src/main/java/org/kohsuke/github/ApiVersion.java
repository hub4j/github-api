package org.kohsuke.github;

/**
 * Different API versions.
 *
 * @author Kohsuke Kawaguchi
 */
enum ApiVersion {
    V2("https://github.com/api/v2/json"),
    V3("https://api.github.com");

    final String url;

    ApiVersion(String url) {
        this.url = url;
    }
}

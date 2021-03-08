package org.kohsuke.github.authorization;

import org.kohsuke.github.BetaApi;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHAppInstallationToken;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Provides an AuthorizationProvider that performs automatic token refresh.
 */
public class OrgAppInstallationAuthorizationProvider extends GitHub.DependentAuthorizationProvider {

    private static final Pattern pattern = Pattern.compile("/repos/(.*)/.*");

    private Map<String, String> latestToken = new HashMap<>();

    @Nonnull
    private Map<String, Instant> validUntil = new HashMap<>();

    /**
     * Provides an AuthorizationProvider that performs automatic token refresh, based on an previously authenticated
     * github client.
     *
     * @param authorizationProvider
     *            A authorization provider that returns a JWT token that can be used to refresh the App Installation
     *            token from GitHub.
     */
    @BetaApi
    @Deprecated
    public OrgAppInstallationAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        super(authorizationProvider);
    }

    @Override
    public String getEncodedAuthorization(URL url) throws IOException {
        synchronized (this) {
            String org = getOrgFromURL(url);
            if (latestToken.get(org) == null || this.validUntil.get(org) == null
                    || Instant.now().isAfter(this.validUntil.get(org))) {
                refreshToken(url);
            }
            return String.format("token %s", latestToken.get(org));
        }
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        return getEncodedAuthorization(null);
    }

    /**
     * Try to figure out what org is this url trying to access so we can use the correct App installation for that org.
     *
     * @param url
     * @return the organization or "" if it cannot be computed
     */
    private String getOrgFromURL(URL url) {
        if (url != null) {
            Matcher matcher = pattern.matcher(url.getPath());
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    private void refreshToken(URL url) throws IOException {
        List<GHAppInstallation> installations = this.gitHub().getApp().listInstallations().asList();
        // take the first one if no one matches
        GHAppInstallation installation = installations.get(0);
        String org = getOrgFromURL(url);
        for (GHAppInstallation ghAppInstallation : installations) {
            if (org.equals(installation.getAccount().getLogin())) {
                System.out.println(
                        String.format("Found installation for path %s: %s", url.getPath(), installation.getHtmlUrl()));
                installation = ghAppInstallation;
                break;
            }
        }
        GHAppInstallationToken ghAppInstallationToken = installation.createToken().create();
        this.validUntil.put(org, ghAppInstallationToken.getExpiresAt().toInstant().minus(Duration.ofMinutes(5)));
        this.latestToken.put(org, Objects.requireNonNull(ghAppInstallationToken.getToken()));
    }
}

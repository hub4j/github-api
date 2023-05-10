package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the GitHub App Api Test
 *
 * @author Daniel Baur
 */
public class GHAppExtendedTest extends AbstractGitHubWireMockTest {

    private static final String APP_SLUG = "ghapi-test-app-4";

    /**
     * Gets the GitHub App by its slug.
     *
     * @throws IOException
     *             An IOException has occurred.
     */
    @Test
    public void getAppBySlugTest() throws IOException {
        GHApp app = gitHub.getApp(APP_SLUG);

        assertThat(app.getId(), is((long) 330762));
        assertThat(app.getSlug(), equalTo(APP_SLUG));
        assertThat(app.getName(), equalTo("GHApi Test app 4"));
        assertThat(app.getExternalUrl(), equalTo("https://github.com/organizations/hub4j-test-org"));
        assertThat(app.getHtmlUrl().toString(), equalTo("https://github.com/apps/ghapi-test-app-4"));
        assertThat(app.getDescription(), equalTo("An app to test the GitHub getApp(slug) method."));
    }

    /**
     * Tests App creation via the App Manifest Flow.
     *
     * The used code defined below was only valid for a short time, meaning that you can not replay the test against the
     * GitHub API. Use the stored wire snapshot for executing those tests.
     *
     * @throws IOException
     *             An IOException has occurred.
     */
    @Test
    public void createAppByManifestFlowTest() throws IOException {
        snapshotNotAllowed();
        GHAppFromManifest appFromManifest = gitHub.createAppFromManifest("46fbe5453b245dee21b96753f80eace209a3cf01");

        assertThat(appFromManifest.getClientId(), equalTo("Iv1.1c63d0b87c03d42e"));
        assertThat(appFromManifest.getWebhookSecret(), equalTo("f4dafa9b05d8248d81f65f0e6cb108cb8bb76a0c"));
        assertThat(appFromManifest.getClientSecret(), equalTo("f4b60603e85b3965492b393bca0809a914dcdf18"));
        assertThat(appFromManifest.getPem(),
                equalTo("-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEA4UT2qvDbMK3hQtvrK7wu7y7B6hypYhsXyD6GN22Bcn3JZdSI\nWm/zhRMH/vKwU5r67YKcJCchHVbvLRWNt911r85D0uLMPIjOkdL+cnSOa5yRhTJy\nI/RhZqx8yoXHSSE5ToKwfPAn3hiv8N2gQEsEpWxdycqPOg7paFsAJ5hjstmS09uU\nKwrFcCQdWuidRnwn6bdgGt+bL9dsvRd4RDoP5sZj5pLNo1y8N9DnFvHihd1rQxQS\nIf9sRgGPDLasNkLvMdxKnsDTsufRBmmw72iaTJXc+EVZw2jYKrOjRVechTMEfbRp\nQRVZw9vysT2XhDB9J4bbJ6NopP/c7JC1ihUJfQIDAQABAoIBAQC0DwubVyncnx+O\n8XnoW2KojBczqfU6Fa3MwS1G4KC3gxOX8WmL4DAmDjA1+IY4TYiEkAF+ZEhzyyki\nQDgm3z1SaOyNg/r75941cRExKzkritpGPSw+0PeJuhWFS6kfKw9DUfL/6nXzcIgx\nXvTYbx4nm5bb1KznG0Q1xYc6HvSR3xb3DcXWXkRX6sMEX4J3M/x0PWxPWnDGvlBJ\nQyDgfqpOa6kra5uSm8qoHtb7httwE/a5NZ9P5jeLk3wXaQmCEl4RDEgSGeR4AOf5\nXVTWP936sVA3vBLd6LmddO3ZEE1HZhlb2XWnxCnGSKL4LoFZE7Jhf2Alp32nm0gm\nwsrQoVgBAoGBAPjxzs3Umlle0EZbfJa8c4rFPOJmMkL07aQu8PAAHiNGcbzzfUmK\n7kfNYXktHKB6pjyjkXEQrLCm9wdqCqI70wJ+AVn6E/0Z1ojPALIxdrjQuNS9xjRo\nCUAQqEXe+IWBZVArgy7t3to7XkAHrj+ky96eAhlsb88c9qiWtS7biXPtAoGBAOen\nYgTe8SbWdBRh7mqDgx9eruB6UCOt8BUlb1uFyt1kpCLZVelw1JYs7hq+roTdKds0\ny9pu+E6I7+g6LsVtjkMqf/VXuY0qomf9hbNE9Yj/Tqty5B4xU+gj01MCm5RRln9M\nKGKCeJAPDB5AEmyidPvLbPp5U6Rniu0Ds+AJ0FnRAoGBAMro/bGTuwNhXs4aP+D1\nVhAkWE4JEqq0zQZoJIba8bW683YZ2WMaVMI9y1djx9OeZOVERYYtGzUZwnxOmMBH\nluSPJDbcuXIxn0X/xAd6fdSCfEUbMfUBX5jSevYImfTn1VaVQOX9iQnEHjx+hi7l\n+i5ICFoEotXkO8CKpr+8vbq5AoGAOCfkZAfjb6XHB/XhhOKSk7UxMWuVJ8EPlSC5\nCPe7AMZX37bN08QtVKZZphQZXE38yo3W6QHDoc4iUipgki2HshKIaGI2sdjm+8yC\nb73Ew8wYNwmn8QXGMF0W6mWUb3UDxaIhnBfCwDFVn7Oqg7kyIKPkrCdjNlR/Ygtm\nvGXEozECgYAFpzntJpUdYN4OxutpNXdnTgN0CJ3TECkk/+0xbTHoWNuMpJ5dR/yQ\n7RLxwcu8CqizXCB750jSgHlWk5GF1yAQzFO9ozjx/mxdPp1PxHaeN/5kmx8VjT8W\nL7zhUMfZLeDMpIbQ/3gyq0EUxxHIEJc2Mx42C9/OY/fkEuZPFSEL2A==\n-----END RSA PRIVATE KEY-----\n"));

    }

}

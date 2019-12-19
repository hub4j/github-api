package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.extras.ImpatientHttpConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Configures connection details and produces {@link GitHub}.
 *
 * @since 1.59
 */
public class GitHubBuilder implements Cloneable {

    // default scoped so unit tests can read them.
    /* private */ String endpoint = GitHub.GITHUB_URL;
    /* private */ String user;
    /* private */ String password;
    /* private */ String oauthToken;
    /* private */ String jwtToken;

    private HttpConnector connector;

    private RateLimitHandler rateLimitHandler = RateLimitHandler.WAIT;
    private AbuseLimitHandler abuseLimitHandler = AbuseLimitHandler.WAIT;

    /**
     * Instantiates a new Git hub builder.
     */
    public GitHubBuilder() {
    }

    /**
     * First check if the credentials are configured in the environment. We use environment first because users are not
     * likely to give required (full) permissions to their default key.
     *
     * If no user is specified it means there is no configuration present, so try using the ~/.github properties file.
     **
     * If there is still no user it means there are no credentials defined and throw an IOException.
     *
     * @return the configured Builder from credentials defined on the system or in the environment. Otherwise returns
     *         null.
     *
     * @throws IOException
     *             If there are no credentials defined in the ~/.github properties file or the process environment.
     */
    static GitHubBuilder fromCredentials() throws IOException {
        Exception cause = null;
        GitHubBuilder builder = null;

        builder = fromEnvironment();

        if (builder.oauthToken != null || builder.user != null || builder.jwtToken != null)
            return builder;

        try {
            builder = fromPropertyFile();

            if (builder.oauthToken != null || builder.user != null || builder.jwtToken != null)
                return builder;
        } catch (FileNotFoundException e) {
            // fall through
            cause = e;
        }
        throw (IOException) new IOException("Failed to resolve credentials from ~/.github or the environment.")
                .initCause(cause);
    }

    /**
     * From environment git hub builder.
     *
     * @param loginVariableName
     *            the login variable name
     * @param passwordVariableName
     *            the password variable name
     * @param oauthVariableName
     *            the oauth variable name
     * @return the git hub builder
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #fromEnvironment()} to pick up standard set of environment variables, so that different
     *             clients of this library will all recognize one consistent set of coordinates.
     */
    public static GitHubBuilder fromEnvironment(String loginVariableName,
            String passwordVariableName,
            String oauthVariableName) throws IOException {
        return fromEnvironment(loginVariableName, passwordVariableName, oauthVariableName, "");
    }

    private static void loadIfSet(String envName, Properties p, String propName) {
        String v = System.getenv(envName);
        if (v != null)
            p.put(propName, v);
    }

    /**
     * From environment git hub builder.
     *
     * @param loginVariableName
     *            the login variable name
     * @param passwordVariableName
     *            the password variable name
     * @param oauthVariableName
     *            the oauth variable name
     * @param endpointVariableName
     *            the endpoint variable name
     * @return the git hub builder
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #fromEnvironment()} to pick up standard set of environment variables, so that different
     *             clients of this library will all recognize one consistent set of coordinates.
     */
    public static GitHubBuilder fromEnvironment(String loginVariableName,
            String passwordVariableName,
            String oauthVariableName,
            String endpointVariableName) throws IOException {
        Properties env = new Properties();
        loadIfSet(loginVariableName, env, "login");
        loadIfSet(passwordVariableName, env, "password");
        loadIfSet(oauthVariableName, env, "oauth");
        loadIfSet(endpointVariableName, env, "endpoint");
        return fromProperties(env);
    }

    /**
     * Creates {@link GitHubBuilder} by picking up coordinates from environment variables.
     *
     * <p>
     * The following environment variables are recognized:
     *
     * <ul>
     * <li>GITHUB_LOGIN: username like 'kohsuke'
     * <li>GITHUB_PASSWORD: raw password
     * <li>GITHUB_OAUTH: OAuth token to login
     * <li>GITHUB_ENDPOINT: URL of the API endpoint
     * <li>GITHUB_JWT: JWT token to login
     * </ul>
     *
     * <p>
     * See class javadoc for the relationship between these coordinates.
     *
     * <p>
     * For backward compatibility, the following environment variables are recognized but discouraged: login, password,
     * oauth
     *
     * @return the git hub builder
     * @throws IOException
     *             the io exception
     */
    public static GitHubBuilder fromEnvironment() throws IOException {
        Properties props = new Properties();
        for (Entry<String, String> e : System.getenv().entrySet()) {
            String name = e.getKey().toLowerCase(Locale.ENGLISH);
            if (name.startsWith("github_"))
                name = name.substring(7);
            props.put(name, e.getValue());
        }
        return fromProperties(props);
    }

    /**
     * From property file git hub builder.
     *
     * @return the git hub builder
     * @throws IOException
     *             the io exception
     */
    public static GitHubBuilder fromPropertyFile() throws IOException {
        File homeDir = new File(System.getProperty("user.home"));
        File propertyFile = new File(homeDir, ".github");
        return fromPropertyFile(propertyFile.getPath());
    }

    /**
     * From property file git hub builder.
     *
     * @param propertyFileName
     *            the property file name
     * @return the git hub builder
     * @throws IOException
     *             the io exception
     */
    public static GitHubBuilder fromPropertyFile(String propertyFileName) throws IOException {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(propertyFileName);
            props.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return fromProperties(props);
    }

    /**
     * From properties git hub builder.
     *
     * @param props
     *            the props
     * @return the git hub builder
     */
    public static GitHubBuilder fromProperties(Properties props) {
        GitHubBuilder self = new GitHubBuilder();
        self.withOAuthToken(props.getProperty("oauth"), props.getProperty("login"));
        self.withJwtToken(props.getProperty("jwt"));
        self.withPassword(props.getProperty("login"), props.getProperty("password"));
        self.withEndpoint(props.getProperty("endpoint", GitHub.GITHUB_URL));
        return self;
    }

    /**
     * With endpoint git hub builder.
     *
     * @param endpoint
     *            The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *            "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <code>/api/v3</code> in the URL. For
     *            historical reasons, this parameter still accepts the bare domain name, but that's considered
     *            deprecated.
     * @return the git hub builder
     */
    public GitHubBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * With password git hub builder.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     * @return the git hub builder
     */
    public GitHubBuilder withPassword(String user, String password) {
        this.user = user;
        this.password = password;
        return this;
    }

    /**
     * With o auth token git hub builder.
     *
     * @param oauthToken
     *            the oauth token
     * @return the git hub builder
     */
    public GitHubBuilder withOAuthToken(String oauthToken) {
        return withOAuthToken(oauthToken, null);
    }

    /**
     * With o auth token git hub builder.
     *
     * @param oauthToken
     *            the oauth token
     * @param user
     *            the user
     * @return the git hub builder
     */
    public GitHubBuilder withOAuthToken(String oauthToken, String user) {
        this.oauthToken = oauthToken;
        this.user = user;
        return this;
    }

    /**
     * Configures {@link GitHubBuilder} with Installation Token generated by the GitHub Application
     *
     * @param appInstallationToken
     *            A string containing the GitHub App installation token
     * @return the configured Builder from given GitHub App installation token.
     * @see GHAppInstallation#createToken(java.util.Map) GHAppInstallation#createToken(java.util.Map)
     */
    public GitHubBuilder withAppInstallationToken(String appInstallationToken) {
        return withOAuthToken(appInstallationToken, "");
    }

    /**
     * With jwt token git hub builder.
     *
     * @param jwtToken
     *            the jwt token
     * @return the git hub builder
     */
    public GitHubBuilder withJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
        return this;
    }

    /**
     * With connector git hub builder.
     *
     * @param connector
     *            the connector
     * @return the git hub builder
     */
    public GitHubBuilder withConnector(HttpConnector connector) {
        this.connector = connector;
        return this;
    }

    /**
     * With rate limit handler git hub builder.
     *
     * @param handler
     *            the handler
     * @return the git hub builder
     */
    public GitHubBuilder withRateLimitHandler(RateLimitHandler handler) {
        this.rateLimitHandler = handler;
        return this;
    }

    /**
     * With abuse limit handler git hub builder.
     *
     * @param handler
     *            the handler
     * @return the git hub builder
     */
    public GitHubBuilder withAbuseLimitHandler(AbuseLimitHandler handler) {
        this.abuseLimitHandler = handler;
        return this;
    }

    /**
     * Configures {@linkplain #withConnector(HttpConnector) connector} that uses HTTP library in JRE but use a specific
     * proxy, instead of the system default one.
     *
     * @param p
     *            the p
     * @return the git hub builder
     */
    public GitHubBuilder withProxy(final Proxy p) {
        return withConnector(new ImpatientHttpConnector(new HttpConnector() {
            public HttpURLConnection connect(URL url) throws IOException {
                return (HttpURLConnection) url.openConnection(p);
            }
        }));
    }

    /**
     * Build git hub.
     *
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public GitHub build() throws IOException {
        return new GitHub(endpoint,
                user,
                oauthToken,
                jwtToken,
                password,
                connector,
                rateLimitHandler,
                abuseLimitHandler);
    }

    @Override
    public GitHubBuilder clone() {
        try {
            return (GitHubBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone should be supported", e);
        }
    }
}

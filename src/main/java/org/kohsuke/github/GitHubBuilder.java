package org.kohsuke.github;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Properties;

/**
 * @since 1.59
 */
public class GitHubBuilder {

    // default scoped so unit tests can read them.
    /* private */ String endpoint = GitHub.GITHUB_URL;
    /* private */ String user;
    /* private */ String password;
    /* private */ String oauthToken;
    
    private HttpConnector connector;

    public GitHubBuilder() {
    }

    /**
     * First check if the credentials are configured using the ~/.github properties file.
     *
     * If no user is specified it means there is no configuration present so check the environment instead.
     *
     * If there is still no user it means there are no credentials defined and throw an IOException.
     *
     * @return the configured Builder from credentials defined on the system or in the environment.
     *
     * @throws IOException If there are no credentials defined in the ~/.github properties file or the process environment.
     */
    public static GitHubBuilder fromCredentials() throws IOException {
    	Exception cause = null;
    	GitHubBuilder builder;

		try {
			builder = fromPropertyFile();

			if (builder.user != null)
	    		return builder;
		} catch (FileNotFoundException e) {
            // fall through
            cause = e;
		}

        builder = fromEnvironment();

        if (builder.user != null)
            return builder;
        else
            throw (IOException)new IOException("Failed to resolve credentials from ~/.github or the environment.").initCause(cause);
    }

    public static GitHubBuilder fromEnvironment(String loginVariableName, String passwordVariableName, String oauthVariableName) throws IOException {
        return fromEnvironment(loginVariableName, passwordVariableName, oauthVariableName, "");
    }

    private static void loadIfSet(String envName, Properties p, String propName) {
        String v = System.getenv(envName);
       	if (v != null)
       		p.put(propName, v);
    }

    public static GitHubBuilder fromEnvironment(String loginVariableName, String passwordVariableName, String oauthVariableName, String endpointVariableName) throws IOException {
    	Properties env = new Properties();
    	loadIfSet(loginVariableName,env,"login");
        loadIfSet(passwordVariableName,env,"password");
        loadIfSet(oauthVariableName,env,"oauth");
        loadIfSet(endpointVariableName,env,"endpoint");
    	return fromProperties(env);
    }
    
    public static GitHubBuilder fromEnvironment() throws IOException {
    	Properties props = new Properties();
    	props.putAll(System.getenv());
        return fromProperties(props);
    }
    
    public static GitHubBuilder fromPropertyFile() throws IOException {
        File homeDir = new File(System.getProperty("user.home"));
        File propertyFile = new File(homeDir, ".github");
        return fromPropertyFile(propertyFile.getPath());
    }

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
    
    public static GitHubBuilder fromProperties(Properties props) {
        GitHubBuilder self = new GitHubBuilder();
        self.withOAuthToken(props.getProperty("oauth"), props.getProperty("login"));
        self.withPassword(props.getProperty("login"), props.getProperty("password"));
        self.withEndpoint(props.getProperty("endpoint", GitHub.GITHUB_URL));
        return self;
    }

    public GitHubBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
    public GitHubBuilder withPassword(String user, String password) {
        this.user = user;
        this.password = password;
        return this;
    }
    public GitHubBuilder withOAuthToken(String oauthToken) {
        return withOAuthToken(oauthToken, null);
    }
    public GitHubBuilder withOAuthToken(String oauthToken, String user) {
        this.oauthToken = oauthToken;
        this.user = user;
        return this;
    }
    public GitHubBuilder withConnector(HttpConnector connector) {
        this.connector = connector;
        return this;
    }

    /**
     * Configures {@linkplain #withConnector(HttpConnector) connector}
     * that uses HTTP library in JRE but use a specific proxy, instead of
     * the system default one.
     */
    public GitHubBuilder withProxy(final Proxy p) {
        return withConnector(new HttpConnector() {
            public HttpURLConnection connect(URL url) throws IOException {
                return (HttpURLConnection) url.openConnection(p);
            }
        });
    }

    public GitHub build() throws IOException {
        return new GitHub(endpoint, user, oauthToken, password, connector);
    }
}

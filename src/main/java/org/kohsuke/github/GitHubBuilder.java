package org.kohsuke.github;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @since 1.59
 */
public class GitHubBuilder {
    private String endpoint = GitHub.GITHUB_URL;
    
    // default scoped so unit tests can read them.
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
    	
    	GitHubBuilder builder;
		try {
			builder = fromPropertyFile();
			
			if (builder.user != null)
	    		return builder;
	    	else {
	    		
	    		// this is the case where the ~/.github file exists but has no content.
	    		
	    		builder = fromEnvironment();
	    		
	    		if (builder.user != null)
	    			return builder;
	    		else
	    			throw new IOException("Failed to resolve credentials from ~/.github or the environment.");
	    	}
	    	
		} catch (FileNotFoundException e) {
			builder = fromEnvironment();
			
			if (builder.user != null)
				return builder;
			else
				throw new IOException("Failed to resolve credentials from ~/.github or the environment.", e);
		}
    
    }
    
    public static GitHubBuilder fromEnvironment(String loginVariableName, String passwordVariableName, String oauthVariableName) throws IOException {
    	
    	
    	Properties env = new Properties();
    	
    	Object loginValue = System.getenv(loginVariableName);
    	
    	if (loginValue != null)
    		env.put("login", loginValue);
    	
    	Object passwordValue = System.getenv(passwordVariableName);
    	
    	if (passwordValue != null)
    		env.put("password", passwordValue);

    	Object oauthValue = System.getenv(oauthVariableName);
    	
    	if (oauthValue != null)
    		env.put("oauth", oauthValue);
    	
    	return fromProperties(env);

    }
    
    public static GitHubBuilder fromEnvironment() throws IOException {
    	
    	Properties props = new Properties();
    	
    	Map<String, String> env = System.getenv();
    	
    	for (Map.Entry<String, String> element : env.entrySet()) {
			
    		props.put(element.getKey(), element.getValue());
		}
    	
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

    public GitHub build() throws IOException {
        return new GitHub(endpoint, user, oauthToken, password, connector);
    }
}

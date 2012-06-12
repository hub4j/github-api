package org.kohsuke.github;

/**
 * Different API versions.
 *
 * @author Kohsuke Kawaguchi
 */
enum ApiVersion {
	
    V3("https://api.?");

    final String templateUrl;

    ApiVersion(String templateUrl) {
        this.templateUrl = templateUrl;
    }
    
    public String getApiVersionBaseUrl(String githubServer) {
    	
    	return templateUrl.replaceFirst("\\?", githubServer);
    		
    }
}

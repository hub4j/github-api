package org.kohsuke.github.exception;

import javax.annotation.CheckForNull;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Request/responce contains useful metadata.
 * Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHFileNotFoundException extends FileNotFoundException {
    protected Map<String, List<String>> responseHeaderFields;

    public GHFileNotFoundException() {
    }

    public GHFileNotFoundException(String s) {
        super(s);
    }

    @CheckForNull
    public Map<String, List<String>> getResponseHeaderFields() {
        return responseHeaderFields;
    }

    public GHFileNotFoundException withResponseHeaderFields(HttpURLConnection urlConnection) {
        this.responseHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}

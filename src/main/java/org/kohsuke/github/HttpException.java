package org.kohsuke.github;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.CheckForNull;

/**
 * {@link IOException} for http exceptions because {@link HttpURLConnection} throws un-discerned {@link IOException} and
 * it can help to know the http response code to decide how to handle an http exceptions.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class HttpException extends GHIOException {
    static final long serialVersionUID = 1L;

    private final int responseCode;
    private final String responseMessage;
    private final String url;

    /**
     * Instantiates a new Http exception.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param responseCode
     *            Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage
     *            Http response message
     * @param url
     *            The url that was invoked
     * @see HttpURLConnection#getResponseCode() HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage() HttpURLConnection#getResponseMessage()
     */
    public HttpException(String message, int responseCode, String responseMessage, String url) {
        super(message);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * Instantiates a new Http exception.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param responseCode
     *            Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage
     *            Http response message
     * @param url
     *            The url that was invoked
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode() HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage() HttpURLConnection#getResponseMessage()
     */
    public HttpException(String message, int responseCode, String responseMessage, String url, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * Instantiates a new Http exception.
     *
     * @param responseCode
     *            Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage
     *            Http response message
     * @param url
     *            The url that was invoked
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode() HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage() HttpURLConnection#getResponseMessage()
     */
    public HttpException(int responseCode, String responseMessage, String url, Throwable cause) {
        super("Server returned HTTP response code: " + responseCode + ", message: '" + responseMessage + "'"
                + " for URL: " + url, cause);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * Instantiates a new Http exception.
     *
     * @param responseCode
     *            Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage
     *            Http response message
     * @param url
     *            The url that was invoked
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode() HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage() HttpURLConnection#getResponseMessage()
     */
    public HttpException(int responseCode, String responseMessage, @CheckForNull URL url, Throwable cause) {
        this(responseCode, responseMessage, url == null ? null : url.toString(), cause);
    }

    /**
     * Instantiates a new Http exception.
     *
     * @param connectorResponse
     *            the connector response to base this on
     */
    public HttpException(GitHubConnectorResponse connectorResponse) {
        this(GitHubResponse.getBodyAsStringOrNull(connectorResponse),
                connectorResponse.statusCode(),
                connectorResponse.header("Status"),
                connectorResponse.request().url().toString());
        this.responseHeaderFields = connectorResponse.allHeaders();
    }

    /**
     * Http response code of the request that cause the exception
     *
     * @return {@code -1} if no code can be discerned.
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Http response message of the request that cause the exception
     *
     * @return {@code null} if no response message can be discerned.
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * The http URL that caused the exception
     *
     * @return url url
     */
    public String getUrl() {
        return url;
    }
}

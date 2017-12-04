/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link IOException} for http exceptions because {@link HttpURLConnection} throws un-discerned
 * {@link IOException} and it can help to know the http response code to decide how to handle an
 * http exceptions.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class HttpException extends IOException {
    static final long serialVersionUID = 1L;

    private final int responseCode;
    private final String responseMessage;
    private final String url;

    /**
     * @param message         The detail message (which is saved for later retrieval
     *                        by the {@link #getMessage()} method)
     * @param responseCode    Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage Http response message
     * @param url             The url that was invoked
     * @see HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage()
     */
    public HttpException(String message, int responseCode, String responseMessage, String url) {
        super(message);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * @param message         The detail message (which is saved for later retrieval
     *                        by the {@link #getMessage()} method)
     * @param responseCode    Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage Http response message
     * @param url             The url that was invoked
     * @param cause           The cause (which is saved for later retrieval by the
     *                        {@link #getCause()} method).  (A null value is permitted,
     *                        and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage()
     */
    public HttpException(String message, int responseCode, String responseMessage, String url, Throwable cause) {
        super(message);
        initCause(cause);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * @param responseCode    Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage Http response message
     * @param url             The url that was invoked
     * @param cause           The cause (which is saved for later retrieval by the
     *                        {@link #getCause()} method).  (A null value is permitted,
     *                        and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage()
     */
    public HttpException(int responseCode, String responseMessage, String url, Throwable cause) {
        super("Server returned HTTP response code: " + responseCode + ", message: '" + responseMessage + "'" +
                " for URL: " + url);
        initCause(cause);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.url = url;
    }

    /**
     * @param responseCode    Http response code. {@code -1} if no code can be discerned.
     * @param responseMessage Http response message
     * @param url             The url that was invoked
     * @param cause           The cause (which is saved for later retrieval by the
     *                        {@link #getCause()} method).  (A null value is permitted,
     *                        and indicates that the cause is nonexistent or unknown.)
     * @see HttpURLConnection#getResponseCode()
     * @see HttpURLConnection#getResponseMessage()
     */
    public HttpException(int responseCode, String responseMessage, @CheckForNull URL url, Throwable cause) {
        this(responseCode, responseMessage, url == null ? null : url.toString(), cause);
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
     * @return url
     */
    public String getUrl() {
        return url;
    }
}

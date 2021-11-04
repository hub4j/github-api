package org.kohsuke.github.extras.okhttp3;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GitHubRequest;
import org.kohsuke.github.ResponseConnector;
import org.kohsuke.github.ResponseInfo;
import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.FINER;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class OkHttpResponseConnector implements ResponseConnector {
    private final OkHttpClient client;

    public OkHttpResponseConnector(OkHttpClient client) {
        this.client = client;
    }

    protected Request.Builder newRequestBuilder() {
        return new Request.Builder();
    }

    @Override
    public ResponseInfo getResponseInfo(GitHubRequest request, AuthorizationProvider authorizationProvider)
            throws IOException {
        Request.Builder builder = newRequestBuilder();
        builder.url(request.url());

        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        if (!request.headers().containsKey("Authorization")) {
            String authorization = authorizationProvider.getEncodedAuthorization();
            if (authorization != null) {
                builder.header("Authorization", authorization);
            }
        }

        for (Map.Entry<String, String> header : request.headers().entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        builder.header("Accept-Encoding", "gzip");
        RequestBody okBody;
        if (request.inBody()) {
            try (InputStream body = request.body()) {
                if (body != null) {
                    okBody = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return MediaType
                                    .parse(defaultString(request.contentType(), "application/x-www-form-urlencoded"));
                        }

                        @Override
                        public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                            bufferedSink.writeAll(Okio.source(body));
                        }
                    };
                } else {
                    okBody = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse(defaultString(request.contentType(), "application/json"));
                        }

                        @Override
                        public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                            Map<String, Object> json = new HashMap<>();
                            for (GitHubRequest.Entry e : request.args()) {
                                json.put(e.key, e.value);
                            }
                            ResponseInfo.getMappingObjectWriter().writeValue(bufferedSink.outputStream(), json);
                        }
                    };
                }
            }
        } else {
            okBody = null;
        }
        builder.method(request.method(), okBody);

        Response response = client.newCall(builder.build()).execute();
        return new ResponseInfo(request, response.code(), response.headers().toMultimap()) {
            @Override
            protected InputStream bodyStream() throws IOException {
                return response.body().byteStream();
            }

            @Override
            protected String errorMessage() {
                try {
                    return response.body().byteString().utf8();
                } catch (IOException e) {
                    LOGGER.log(FINER, "Ignored exception get error message", e);
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                response.close();
            }
        };
    }

    private static final Logger LOGGER = Logger.getLogger(OkHttpResponseConnector.class.getName());
}

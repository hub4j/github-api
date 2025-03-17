package org.kohsuke.github.connector;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.connector.GitHubConnectorResponse.ByteArrayResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

/**
 * Test GitHubConnectorResponse
 */
public class GitHubConnectorResponseTest extends AbstractGitHubWireMockTest {

    /**
     * Test basic body stream.
     *
     * @throws Exception
     *             for failures
     */
    @Test
    public void testBodyStream() throws Exception {
        Exception e;
        GitHubConnectorResponse response = new CustomBodyGitHubConnectorResponse(200,
                new ByteBufferBackedInputStream(ByteBuffer.wrap("Hello!".getBytes(StandardCharsets.UTF_8))));
        InputStream stream = response.bodyStream();
        assertThat(stream, isA(ByteBufferBackedInputStream.class));
        String bodyString = IOUtils.toString(stream, StandardCharsets.UTF_8);
        assertThat(bodyString, equalTo("Hello!"));

        // Cannot change to rereadable
        e = Assert.assertThrows(RuntimeException.class, () -> response.setBodyStreamRereadable());
        assertThat(e.getMessage(), equalTo("bodyStream() already called in read-once mode"));

        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response body not rereadable"));
        response.close();
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response is closed"));
    }

    /**
     * Test rereadable body stream.
     *
     * @throws Exception
     *             for failures
     */
    @Test
    public void tesBodyStream_rereadable() throws Exception {
        Exception e;
        GitHubConnectorResponse response = new CustomBodyGitHubConnectorResponse(404,
                new ByteBufferBackedInputStream(ByteBuffer.wrap("Hello!".getBytes(StandardCharsets.UTF_8))));
        InputStream stream = response.bodyStream();
        assertThat(stream, isA(ByteArrayInputStream.class));
        String bodyString = IOUtils.toString(stream, StandardCharsets.UTF_8);
        assertThat(bodyString, equalTo("Hello!"));

        // Buffered response can be read multiple times
        bodyString = IOUtils.toString(response.bodyStream(), StandardCharsets.UTF_8);
        assertThat(bodyString, equalTo("Hello!"));

        // should have no effect if already rereadable
        response.setBodyStreamRereadable();

        response.close();
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response is closed"));
    }

    /**
     * Test forced rereadable body stream.
     *
     * @throws Exception
     *             for failures
     */
    @Test
    public void tesBodyStream_forced() throws Exception {
        Exception e;
        GitHubConnectorResponse response = new CustomBodyGitHubConnectorResponse(200,
                new ByteBufferBackedInputStream(ByteBuffer.wrap("Hello!".getBytes(StandardCharsets.UTF_8))));
        // 200 status would be streamed body, force to buffered
        response.setBodyStreamRereadable();

        InputStream stream = response.bodyStream();
        assertThat(stream, isA(ByteArrayInputStream.class));
        String bodyString = IOUtils.toString(stream, StandardCharsets.UTF_8);
        assertThat(bodyString, equalTo("Hello!"));

        // Buffered response can be read multiple times
        bodyString = IOUtils.toString(response.bodyStream(), StandardCharsets.UTF_8);
        assertThat(bodyString, equalTo("Hello!"));

        response.close();
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response is closed"));
    }

    /**
     * Test null body stream.
     *
     * @throws Exception
     *             for failures
     */
    @Test
    public void testBodyStream_null() throws Exception {
        Exception e;
        GitHubConnectorResponse response = new CustomBodyGitHubConnectorResponse(200, null);
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response body missing, stream null"));

        // Cannot change to rereadable
        e = Assert.assertThrows(RuntimeException.class, () -> response.setBodyStreamRereadable());
        assertThat(e.getMessage(), equalTo("bodyStream() already called in read-once mode"));

        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response body not rereadable"));

        response.close();
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response is closed"));
    }

    /**
     * Test null rereadable body stream.
     *
     * @throws Exception
     *             for failures
     */
    @Test
    public void testBodyStream_null_buffered() throws Exception {
        Exception e;
        GitHubConnectorResponse response = new CustomBodyGitHubConnectorResponse(404, null);
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response body missing, stream null"));
        // Buffered response can be read multiple times
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response body missing, stream null"));

        // force should have no effect after first read attempt
        response.setBodyStreamRereadable();

        response.close();
        e = Assert.assertThrows(IOException.class, () -> response.bodyStream());
        assertThat(e.getMessage(), equalTo("Response is closed"));
    }

    // Extend ByteArrayResponse to preserve test coverage
    private static class CustomBodyGitHubConnectorResponse extends ByteArrayResponse {
        private final InputStream stream;

        CustomBodyGitHubConnectorResponse(int statusCode, InputStream stream) {
            super(EMPTY_REQUEST, statusCode, new HashMap<>());
            this.stream = stream;
        }

        @Override
        protected InputStream rawBodyStream() throws IOException {
            return stream;
        }
    }

    /**
     * Empty request for response testing.
     */
    public static final GitHubConnectorRequest EMPTY_REQUEST = new GitHubConnectorRequest() {
        @NotNull
        @Override
        public String method() {
            return null;
        }

        @NotNull
        @Override
        public Map<String, List<String>> allHeaders() {
            return null;
        }

        @Nullable
        @Override
        public String header(String name) {
            return null;
        }

        @Nullable
        @Override
        public String contentType() {
            return null;
        }

        @Nullable
        @Override
        public InputStream body() {
            return null;
        }

        @NotNull
        @Override
        public URL url() {
            return null;
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    };

}

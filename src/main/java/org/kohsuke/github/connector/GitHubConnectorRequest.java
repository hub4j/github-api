package org.kohsuke.github.connector;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface GitHubConnectorRequest {
    @Nonnull
    String method();

    @Nonnull
    Map<String, List<String>> allHeaders();

    @CheckForNull
    String header(String name);

    @CheckForNull
    String contentType();

    @CheckForNull
    InputStream body();

    @Nonnull
    URL url();

    boolean hasBody();

    @Nonnull
    Map<String, Object> injectedMappingValues();
}

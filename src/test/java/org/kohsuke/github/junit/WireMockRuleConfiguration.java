package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionLoader;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import wiremock.com.google.common.base.Optional;
import wiremock.com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WireMockRuleConfiguration implements Options {

    private final Options parent;
    private final String childDirectory;
    private MappingsSource mappingsSource;
    private Map<String, Extension> extensions = Maps.newLinkedHashMap();

    WireMockRuleConfiguration() {
        this(WireMockConfiguration.options(), null);
    }

    WireMockRuleConfiguration(Options parent, String childDirectory, Extension... extensionInstances) {
        this.parent = parent;
        this.childDirectory = childDirectory;
        this.extensions.putAll(ExtensionLoader.asMap(Arrays.asList(extensionInstances)));
    }

    public static WireMockRuleConfiguration wireMockConfig() {
        return new WireMockRuleConfiguration();
    }

    public static WireMockRuleConfiguration options() {
        return wireMockConfig();
    }

    public WireMockRuleConfiguration forChildPath(String childPath) {
        return new WireMockRuleConfiguration(this, childPath);
    }

    private MappingsSource getMappingsSource() {
        if (this.mappingsSource == null) {
            this.mappingsSource = new JsonFileMappingsSource(this.filesRoot().child("mappings"));
        }

        return this.mappingsSource;
    }

    public FileSource filesRoot() {
        return childDirectory != null ? parent.filesRoot().child(childDirectory) : parent.filesRoot();
    }

    public MappingsLoader mappingsLoader() {
        return this.getMappingsSource();
    }

    public MappingsSaver mappingsSaver() {
        return this.getMappingsSource();
    }

    public WireMockRuleConfiguration mappingSource(MappingsSource mappingsSource) {
        this.mappingsSource = mappingsSource;
        return this;
    }

    public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {
        Map<String, T> result = Maps.newLinkedHashMap(this.parent.extensionsOfType(extensionType));
        result.putAll((Map<String, T>) Maps.filterEntries(this.extensions,
                ExtensionLoader.valueAssignableFrom(extensionType)));
        return result;
    }

    // Simple wrappers

    public int portNumber() {
        return parent.portNumber();
    }

    public boolean getHttpDisabled() {
        return parent.getHttpDisabled();
    }

    public int containerThreads() {
        return parent.containerThreads();
    }

    public HttpsSettings httpsSettings() {
        return parent.httpsSettings();
    }

    public JettySettings jettySettings() {
        return parent.jettySettings();
    }

    public boolean browserProxyingEnabled() {
        return parent.browserProxyingEnabled();
    }

    public BrowserProxySettings browserProxySettings() {
        return parent.browserProxySettings();
    }

    public ProxySettings proxyVia() {
        return parent.proxyVia();
    }

    public Notifier notifier() {
        return parent.notifier();
    }

    public boolean requestJournalDisabled() {
        return parent.requestJournalDisabled();
    }

    public Optional<Integer> maxRequestJournalEntries() {
        return parent.maxRequestJournalEntries();
    }

    public String bindAddress() {
        return parent.bindAddress();
    }

    public List<CaseInsensitiveKey> matchingHeaders() {
        return parent.matchingHeaders();
    }

    public HttpServerFactory httpServerFactory() {
        return parent.httpServerFactory();
    }

    public ThreadPoolFactory threadPoolFactory() {
        return parent.threadPoolFactory();
    }

    public boolean shouldPreserveHostHeader() {
        return parent.shouldPreserveHostHeader();
    }

    public String proxyHostHeader() {
        return parent.proxyHostHeader();
    }

    public WiremockNetworkTrafficListener networkTrafficListener() {
        return parent.networkTrafficListener();
    }

    public Authenticator getAdminAuthenticator() {
        return parent.getAdminAuthenticator();
    }

    public boolean getHttpsRequiredForAdminApi() {
        return parent.getHttpsRequiredForAdminApi();
    }

    public NotMatchedRenderer getNotMatchedRenderer() {
        return parent.getNotMatchedRenderer();
    }

    public AsynchronousResponseSettings getAsynchronousResponseSettings() {
        return parent.getAsynchronousResponseSettings();
    }

    public ChunkedEncodingPolicy getChunkedEncodingPolicy() {
        return parent.getChunkedEncodingPolicy();
    }

    public boolean getGzipDisabled() {
        return parent.getGzipDisabled();
    }

    public boolean getStubRequestLoggingDisabled() {
        return parent.getStubRequestLoggingDisabled();
    }

    public boolean getStubCorsEnabled() {
        return parent.getStubCorsEnabled();
    }
}

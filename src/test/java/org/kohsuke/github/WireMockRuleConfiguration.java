package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionDeclarations;
import com.github.tomakehurst.wiremock.extension.ExtensionLoader;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.store.Stores;
import wiremock.com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class WireMockRuleConfiguration.
 */
public class WireMockRuleConfiguration implements Options {

    private final Options parent;
    private final String childDirectory;
    private MappingsSource mappingsSource;
    private Map<String, Extension> extensions = Maps.newLinkedHashMap();

    /**
     * Instantiates a new wire mock rule configuration.
     */
    WireMockRuleConfiguration() {
        this(WireMockConfiguration.options(), null);
    }

    /**
     * Instantiates a new wire mock rule configuration.
     *
     * @param parent
     *            the parent
     * @param childDirectory
     *            the child directory
     * @param extensionInstances
     *            the extension instances
     */
    WireMockRuleConfiguration(Options parent, String childDirectory, Extension... extensionInstances) {
        this.parent = parent;
        this.childDirectory = childDirectory;
        // this.extensions.putAll(ExtensionLoader.asMap(Arrays.asList(extensionInstances)));
    }

    /**
     * Wire mock config.
     *
     * @return the wire mock rule configuration
     */
    public static WireMockRuleConfiguration wireMockConfig() {
        return new WireMockRuleConfiguration();
    }

    /**
     * Options.
     *
     * @return the wire mock rule configuration
     */
    public static WireMockRuleConfiguration options() {
        return wireMockConfig();
    }

    /**
     * For child path.
     *
     * @param childPath
     *            the child path
     * @return the wire mock rule configuration
     */
    public WireMockRuleConfiguration forChildPath(String childPath) {
        return new WireMockRuleConfiguration(this, childPath);
    }

    private MappingsSource getMappingsSource() {
        if (this.mappingsSource == null) {
            this.mappingsSource = new JsonFileMappingsSource(this.filesRoot().child("mappings"), new FilenameMaker());
        }

        return this.mappingsSource;
    }

    /**
     * Files root.
     *
     * @return the file source
     */
    public FileSource filesRoot() {
        return childDirectory != null ? parent.filesRoot().child(childDirectory) : parent.filesRoot();
    }

    /**
     * Mappings loader.
     *
     * @return the mappings loader
     */
    public MappingsLoader mappingsLoader() {
        return this.getMappingsSource();
    }

    /**
     * Mappings saver.
     *
     * @return the mappings saver
     */
    public MappingsSaver mappingsSaver() {
        return this.getMappingsSource();
    }

    /**
     * Mapping source.
     *
     * @param mappingsSource
     *            the mappings source
     * @return the wire mock rule configuration
     */
    public WireMockRuleConfiguration mappingSource(MappingsSource mappingsSource) {
        this.mappingsSource = mappingsSource;
        return this;
    }

    /**
     * Extensions of type.
     *
     * @param <T>
     *            the generic type
     * @param extensionType
     *            the extension type
     * @return the map
     */
    // public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {
    // parent.getDeclaredExtensions()
    // Map<String, T> result = Maps.newLinkedHashMap(this.parent.exextensionsOfType(extensionType));
    // result.putAll((Map<String, T>) Maps.filterEntries(this.extensions,
    // ExtensionLoader.valueAssignableFrom(extensionType)));
    // return result;
    // }

    // Simple wrappers

    /**
     * Port number.
     *
     * @return the int
     */
    public int portNumber() {
        return parent.portNumber();
    }

    /**
     * Gets the http disabled.
     *
     * @return the http disabled
     */
    public boolean getHttpDisabled() {
        return parent.getHttpDisabled();
    }

    /**
     * Container threads.
     *
     * @return the int
     */
    public int containerThreads() {
        return parent.containerThreads();
    }

    /**
     * Https settings.
     *
     * @return the https settings
     */
    public HttpsSettings httpsSettings() {
        return parent.httpsSettings();
    }

    /**
     * Jetty settings.
     *
     * @return the jetty settings
     */
    public JettySettings jettySettings() {
        return parent.jettySettings();
    }

    /**
     * Browser proxying enabled.
     *
     * @return true, if successful
     */
    public boolean browserProxyingEnabled() {
        return parent.browserProxyingEnabled();
    }

    /**
     * Browser proxy settings.
     *
     * @return the browser proxy settings
     */
    public BrowserProxySettings browserProxySettings() {
        return parent.browserProxySettings();
    }

    /**
     * Proxy via.
     *
     * @return the proxy settings
     */
    public ProxySettings proxyVia() {
        return parent.proxyVia();
    }

    /**
     * Notifier.
     *
     * @return the notifier
     */
    public Notifier notifier() {
        return parent.notifier();
    }

    /**
     * Request journal disabled.
     *
     * @return true, if successful
     */
    public boolean requestJournalDisabled() {
        return parent.requestJournalDisabled();
    }

    /**
     * Max request journal entries.
     *
     * @return the optional
     */
    public java.util.Optional<Integer> maxRequestJournalEntries() {
        return parent.maxRequestJournalEntries();
    }

    /**
     * Bind address.
     *
     * @return the string
     */
    public String bindAddress() {
        return parent.bindAddress();
    }

    /**
     * Matching headers.
     *
     * @return the list
     */
    public List<CaseInsensitiveKey> matchingHeaders() {
        return parent.matchingHeaders();
    }

    /**
     * Http server factory.
     *
     * @return the http server factory
     */
    public HttpServerFactory httpServerFactory() {
        return parent.httpServerFactory();
    }

    /**
     * Thread pool factory.
     *
     * @return the thread pool factory
     */
    public ThreadPoolFactory threadPoolFactory() {
        return parent.threadPoolFactory();
    }

    /**
     * Should preserve host header.
     *
     * @return true, if successful
     */
    public boolean shouldPreserveHostHeader() {
        return parent.shouldPreserveHostHeader();
    }

    /**
     * Proxy host header.
     *
     * @return the string
     */
    public String proxyHostHeader() {
        return parent.proxyHostHeader();
    }

    /**
     * Network traffic listener.
     *
     * @return the wiremock network traffic listener
     */
    public WiremockNetworkTrafficListener networkTrafficListener() {
        return parent.networkTrafficListener();
    }

    /**
     * Gets the admin authenticator.
     *
     * @return the admin authenticator
     */
    public Authenticator getAdminAuthenticator() {
        return parent.getAdminAuthenticator();
    }

    /**
     * Gets the https required for admin api.
     *
     * @return the https required for admin api
     */
    public boolean getHttpsRequiredForAdminApi() {
        return parent.getHttpsRequiredForAdminApi();
    }

    /**
     * Gets the asynchronous response settings.
     *
     * @return the asynchronous response settings
     */
    public AsynchronousResponseSettings getAsynchronousResponseSettings() {
        return parent.getAsynchronousResponseSettings();
    }

    /**
     * Gets the chunked encoding policy.
     *
     * @return the chunked encoding policy
     */
    public ChunkedEncodingPolicy getChunkedEncodingPolicy() {
        return parent.getChunkedEncodingPolicy();
    }

    /**
     * Gets the gzip disabled.
     *
     * @return the gzip disabled
     */
    public boolean getGzipDisabled() {
        return parent.getGzipDisabled();
    }

    /**
     * Gets the stub request logging disabled.
     *
     * @return the stub request logging disabled
     */
    public boolean getStubRequestLoggingDisabled() {
        return parent.getStubRequestLoggingDisabled();
    }

    /**
     * Gets the stub cors enabled.
     *
     * @return the stub cors enabled
     */
    public boolean getStubCorsEnabled() {
        return parent.getStubCorsEnabled();
    }

    /**
     * Timeout.
     *
     * @return the long
     */
    public long timeout() {
        return parent.timeout();
    }

    /**
     * Gets the disable optimize xml factories loading.
     *
     * @return the disable optimize xml factories loading
     */
    public boolean getDisableOptimizeXmlFactoriesLoading() {
        return parent.getDisableOptimizeXmlFactoriesLoading();
    }

    /**
     * Gets the disable strict http headers.
     *
     * @return the disable strict http headers
     */
    public boolean getDisableStrictHttpHeaders() {
        return parent.getDisableStrictHttpHeaders();
    }

    /**
     * Gets the data truncation settings.
     *
     * @return the data truncation settings
     */
    public DataTruncationSettings getDataTruncationSettings() {
        return parent.getDataTruncationSettings();
    }

    /**
     * Gets the network address rules.
     *
     * @return the network address rules
     */
    public NetworkAddressRules getProxyTargetRules() {
        return parent.getProxyTargetRules();
    }

    @Override
    public boolean getHttp2PlainDisabled() {
        return parent.getHttp2PlainDisabled();
    }

    @Override
    public boolean getHttp2TlsDisabled() {
        return parent.getHttp2TlsDisabled();
    }

    @Override
    public Stores getStores() {
        return parent.getStores();
    }

    @Override
    public FilenameMaker getFilenameMaker() {
        return parent.getFilenameMaker();
    }

    @Override
    public boolean shouldPreserveUserAgentProxyHeader() {
        return parent.shouldPreserveUserAgentProxyHeader();
    }

    @Override
    public HttpClientFactory httpClientFactory() {
        return parent.httpClientFactory();
    }

    @Override
    public ExtensionDeclarations getDeclaredExtensions() {
        ExtensionDeclarations declarations = parent.getDeclaredExtensions();

        for (String extentionName : extensions.keySet()) {
            if (!declarations.getInstances().containsKey(extentionName)) {
                declarations.add(extensions.get(extentionName));
            }
        }
        return declarations;
    }

    @Override
    public boolean isExtensionScanningEnabled() {
        return parent.isExtensionScanningEnabled();
    }

    @Override
    public int proxyTimeout() {
        return parent.proxyTimeout();
    }

    @Override
    public int getMaxHttpClientConnections() {
        return parent.getMaxHttpClientConnections();
    }

    @Override
    public boolean getResponseTemplatingEnabled() {
        return parent.getResponseTemplatingEnabled();
    }

    @Override
    public boolean getResponseTemplatingGlobal() {
        return parent.getResponseTemplatingGlobal();
    }

    @Override
    public Long getMaxTemplateCacheEntries() {
        return parent.getMaxTemplateCacheEntries();
    }

    @Override
    public Set<String> getTemplatePermittedSystemKeys() {
        return parent.getTemplatePermittedSystemKeys();
    }

    @Override
    public boolean getTemplateEscapingDisabled() {
        return parent.getTemplateEscapingDisabled();
    }

    @Override
    public Set<String> getSupportedProxyEncodings() {
        return parent.getSupportedProxyEncodings();
    }

    @Override
    public boolean getDisableConnectionReuse() {
        return parent.getDisableConnectionReuse();

    }
}

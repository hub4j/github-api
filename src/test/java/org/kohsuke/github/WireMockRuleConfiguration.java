package org.kohsuke.github;

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

/**
 * The Class WireMockRuleConfiguration.
 */
public class WireMockRuleConfiguration implements Options {

    /**
     * Options.
     *
     * @return the wire mock rule configuration
     */
    public static WireMockRuleConfiguration options() {
        return wireMockConfig();
    }
    /**
     * Wire mock config.
     *
     * @return the wire mock rule configuration
     */
    public static WireMockRuleConfiguration wireMockConfig() {
        return new WireMockRuleConfiguration();
    }
    private final String childDirectory;
    private Map<String, Extension> extensions = Maps.newLinkedHashMap();

    private MappingsSource mappingsSource;

    private final Options parent;

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
        this.extensions.putAll(ExtensionLoader.asMap(Arrays.asList(extensionInstances)));
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
     * Browser proxy settings.
     *
     * @return the browser proxy settings
     */
    public BrowserProxySettings browserProxySettings() {
        return parent.browserProxySettings();
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
     * Container threads.
     *
     * @return the int
     */
    public int containerThreads() {
        return parent.containerThreads();
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
    public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {
        Map<String, T> result = Maps.newLinkedHashMap(this.parent.extensionsOfType(extensionType));
        result.putAll((Map<String, T>) Maps.filterEntries(this.extensions,
                ExtensionLoader.valueAssignableFrom(extensionType)));
        return result;
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
     * For child path.
     *
     * @param childPath
     *            the child path
     * @return the wire mock rule configuration
     */
    public WireMockRuleConfiguration forChildPath(String childPath) {
        return new WireMockRuleConfiguration(this, childPath);
    }

    // Simple wrappers

    /**
     * Gets the admin authenticator.
     *
     * @return the admin authenticator
     */
    public Authenticator getAdminAuthenticator() {
        return parent.getAdminAuthenticator();
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
     * Gets the data truncation settings.
     *
     * @return the data truncation settings
     */
    public DataTruncationSettings getDataTruncationSettings() {
        return parent.getDataTruncationSettings();
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
     * Gets the gzip disabled.
     *
     * @return the gzip disabled
     */
    public boolean getGzipDisabled() {
        return parent.getGzipDisabled();
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
     * Gets the https required for admin api.
     *
     * @return the https required for admin api
     */
    public boolean getHttpsRequiredForAdminApi() {
        return parent.getHttpsRequiredForAdminApi();
    }

    /**
     * Gets the not matched renderer.
     *
     * @return the not matched renderer
     */
    public NotMatchedRenderer getNotMatchedRenderer() {
        return parent.getNotMatchedRenderer();
    }

    /**
     * Gets the network address rules.
     *
     * @return the network address rules
     */
    public NetworkAddressRules getProxyTargetRules() {
        return parent.getProxyTargetRules();
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
     * Gets the stub request logging disabled.
     *
     * @return the stub request logging disabled
     */
    public boolean getStubRequestLoggingDisabled() {
        return parent.getStubRequestLoggingDisabled();
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
     * Matching headers.
     *
     * @return the list
     */
    public List<CaseInsensitiveKey> matchingHeaders() {
        return parent.matchingHeaders();
    }

    /**
     * Max request journal entries.
     *
     * @return the optional
     */
    public Optional<Integer> maxRequestJournalEntries() {
        return parent.maxRequestJournalEntries();
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
     * Notifier.
     *
     * @return the notifier
     */
    public Notifier notifier() {
        return parent.notifier();
    }

    /**
     * Port number.
     *
     * @return the int
     */
    public int portNumber() {
        return parent.portNumber();
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
     * Proxy via.
     *
     * @return the proxy settings
     */
    public ProxySettings proxyVia() {
        return parent.proxyVia();
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
     * Should preserve host header.
     *
     * @return true, if successful
     */
    public boolean shouldPreserveHostHeader() {
        return parent.shouldPreserveHostHeader();
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
     * Timeout.
     *
     * @return the long
     */
    public long timeout() {
        return parent.timeout();
    }

    private MappingsSource getMappingsSource() {
        if (this.mappingsSource == null) {
            this.mappingsSource = new JsonFileMappingsSource(this.filesRoot().child("mappings"));
        }

        return this.mappingsSource;
    }
}

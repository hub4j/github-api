package org.kohsuke.github;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.*;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * The Class WireMockRule.
 *
 * @author Liam Newman
 */
public class WireMockRule implements MethodRule, TestRule, Container, Stubbing, Admin {

    private boolean failOnUnmatchedRequests;
    private String methodName = null;
    private final Options options;

    private WireMockServer wireMockServer;

    /**
     * Instantiates a new wire mock rule.
     */
    public WireMockRule() {
        this(WireMockRuleConfiguration.wireMockConfig());
    }

    /**
     * Instantiates a new wire mock rule.
     *
     * @param options
     *            the options
     */
    public WireMockRule(Options options) {
        this(options, true);
    }

    /**
     * Instantiates a new wire mock rule.
     *
     * @param options
     *            the options
     * @param failOnUnmatchedRequests
     *            the fail on unmatched requests
     */
    public WireMockRule(Options options, boolean failOnUnmatchedRequests) {
        this.options = options;
        this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    }

    /**
     * Instantiates a new wire mock rule.
     *
     * @param port
     *            the port
     */
    public WireMockRule(int port) {
        this(WireMockConfiguration.wireMockConfig().port(port));
    }

    /**
     * Instantiates a new wire mock rule.
     *
     * @param port
     *            the port
     * @param httpsPort
     *            the https port
     */
    public WireMockRule(int port, Integer httpsPort) {
        this(WireMockConfiguration.wireMockConfig().port(port).httpsPort(httpsPort));
    }

    /**
     * Adds the mock service request listener.
     *
     * @param listener
     *            the listener
     */
    public void addMockServiceRequestListener(RequestListener listener) {
        wireMockServer.addMockServiceRequestListener(listener);
    }

    /**
     * Adds the stub mapping.
     *
     * @param stubMapping
     *            the stub mapping
     */
    public void addStubMapping(StubMapping stubMapping) {
        wireMockServer.addStubMapping(stubMapping);
    }

    /**
     * Apply.
     *
     * @param base
     *            the base
     * @param description
     *            the description
     * @return the statement
     */
    public Statement apply(Statement base, Description description) {
        return this.apply(base, description.getMethodName());
    }

    /**
     * Apply.
     *
     * @param base
     *            the base
     * @param method
     *            the method
     * @param target
     *            the target
     * @return the statement
     */
    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return this.apply(base, method.getName());
    }

    /**
     * Base url.
     *
     * @return the string
     */
    public String baseUrl() {
        return wireMockServer.baseUrl();
    }

    /**
     * Count requests matching.
     *
     * @param requestPattern
     *            the request pattern
     * @return the verification result
     */
    public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
        return wireMockServer.countRequestsMatching(requestPattern);
    }

    /**
     * Edits the stub.
     *
     * @param mappingBuilder
     *            the mapping builder
     */
    public void editStub(MappingBuilder mappingBuilder) {
        wireMockServer.editStub(mappingBuilder);
    }

    /**
     * Edits the stub mapping.
     *
     * @param stubMapping
     *            the stub mapping
     */
    public void editStubMapping(StubMapping stubMapping) {
        wireMockServer.editStubMapping(stubMapping);
    }

    /**
     * Enable record mappings.
     *
     * @param mappingsFileSource
     *            the mappings file source
     * @param filesFileSource
     *            the files file source
     */
    public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
        wireMockServer.enableRecordMappings(mappingsFileSource, filesFileSource);
    }

    /**
     * Find all.
     *
     * @param requestPatternBuilder
     *            the request pattern builder
     * @return the list
     */
    public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return wireMockServer.findAll(requestPatternBuilder);
    }

    /**
     * Find all near misses for.
     *
     * @param requestPatternBuilder
     *            the request pattern builder
     * @return the list
     */
    public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
        return wireMockServer.findAllNearMissesFor(requestPatternBuilder);
    }

    /**
     * Find all stubs by metadata.
     *
     * @param pattern
     *            the pattern
     * @return the list stub mappings result
     */
    public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
        return wireMockServer.findAllStubsByMetadata(pattern);
    }

    /**
     * Find all unmatched requests.
     *
     * @return the list
     */
    public List<LoggedRequest> findAllUnmatchedRequests() {
        return wireMockServer.findAllUnmatchedRequests();
    }

    /**
     * Find near misses for.
     *
     * @param loggedRequest
     *            the logged request
     * @return the list
     */
    public List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
        return wireMockServer.findNearMissesFor(loggedRequest);
    }

    /**
     * Find near misses for all unmatched requests.
     *
     * @return the list
     */
    public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
        return wireMockServer.findNearMissesForAllUnmatchedRequests();
    }

    /**
     * Find near misses for unmatched requests.
     *
     * @return the find near misses result
     */
    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
        return wireMockServer.findNearMissesForUnmatchedRequests();
    }

    /**
     * Find requests matching.
     *
     * @param requestPattern
     *            the request pattern
     * @return the find requests result
     */
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        return wireMockServer.findRequestsMatching(requestPattern);
    }

    /**
     * Find stub mappings by metadata.
     *
     * @param pattern
     *            the pattern
     * @return the list
     */
    public List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern) {
        return wireMockServer.findStubMappingsByMetadata(pattern);
    }

    /**
     * Find top near misses for.
     *
     * @param loggedRequest
     *            the logged request
     * @return the find near misses result
     */
    public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
        return wireMockServer.findTopNearMissesFor(loggedRequest);
    }

    /**
     * Find top near misses for.
     *
     * @param requestPattern
     *            the request pattern
     * @return the find near misses result
     */
    public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
        return wireMockServer.findTopNearMissesFor(requestPattern);
    }

    /**
     * Find unmatched requests.
     *
     * @return the find requests result
     */
    public FindRequestsResult findUnmatchedRequests() {
        return wireMockServer.findUnmatchedRequests();
    }

    /**
     * Gets the all scenarios.
     *
     * @return the all scenarios
     */
    public GetScenariosResult getAllScenarios() {
        return wireMockServer.getAllScenarios();
    }

    /**
     * Gets the all serve events.
     *
     * @return the all serve events
     */
    public List<ServeEvent> getAllServeEvents() {
        return wireMockServer.getAllServeEvents();
    }

    /**
     * Gets the global settings.
     *
     * @return the global settings
     */
    public GetGlobalSettingsResult getGlobalSettings() {
        return wireMockServer.getGlobalSettings();
    }

    /**
     * Gets the global settings holder.
     *
     * @return the global settings holder
     */
    public GlobalSettingsHolder getGlobalSettingsHolder() {
        return wireMockServer.getGlobalSettingsHolder();
    }

    /**
     * Gets the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    public Options getOptions() {
        return wireMockServer.getOptions();
    }

    /**
     * Gets the recording status.
     *
     * @return the recording status
     */
    public RecordingStatusResult getRecordingStatus() {
        return wireMockServer.getRecordingStatus();
    }

    /**
     * Gets the serve events.
     *
     * @return the serve events
     */
    public GetServeEventsResult getServeEvents() {
        return wireMockServer.getServeEvents();
    }

    /**
     * Gets the serve events.
     *
     * @param serveEventQuery
     *            the serve event query
     * @return the serve events
     */
    public GetServeEventsResult getServeEvents(ServeEventQuery serveEventQuery) {
        return wireMockServer.getServeEvents(serveEventQuery);
    }

    /**
     * Gets the served stub.
     *
     * @param id
     *            the id
     * @return the served stub
     */
    public SingleServedStubResult getServedStub(UUID id) {
        return wireMockServer.getServedStub(id);
    }

    /**
     * Gets the single stub mapping.
     *
     * @param id
     *            the id
     * @return the single stub mapping
     */
    public StubMapping getSingleStubMapping(UUID id) {
        return wireMockServer.getSingleStubMapping(id);
    }

    /**
     * Gets the stub mapping.
     *
     * @param id
     *            the id
     * @return the stub mapping
     */
    public SingleStubMappingResult getStubMapping(UUID id) {
        return wireMockServer.getStubMapping(id);
    }

    /**
     * Gets the stub mappings.
     *
     * @return the stub mappings
     */
    public List<StubMapping> getStubMappings() {
        return wireMockServer.getStubMappings();
    }

    /**
     * Given that.
     *
     * @param mappingBuilder
     *            the mapping builder
     * @return the stub mapping
     */
    public StubMapping givenThat(MappingBuilder mappingBuilder) {
        return wireMockServer.givenThat(mappingBuilder);
    }

    /**
     * Https port.
     *
     * @return the int
     */
    public int httpsPort() {
        return wireMockServer.httpsPort();
    }

    /**
     * Import stubs.
     *
     * @param stubImport
     *            the stub import
     */
    public void importStubs(StubImport stubImport) {
        wireMockServer.importStubs(stubImport);
    }

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return wireMockServer.isRunning();
    }

    /**
     * List all stub mappings.
     *
     * @return the list stub mappings result
     */
    public ListStubMappingsResult listAllStubMappings() {
        return wireMockServer.listAllStubMappings();
    }

    /**
     * Load mappings using.
     *
     * @param mappingsLoader
     *            the mappings loader
     */
    public void loadMappingsUsing(MappingsLoader mappingsLoader) {
        wireMockServer.loadMappingsUsing(mappingsLoader);
    }

    /**
     * Port.
     *
     * @return the int
     */
    public int port() {
        return wireMockServer.port();
    }

    /**
     * Removes the serve event.
     *
     * @param uuid
     *            the uuid
     */
    public void removeServeEvent(UUID uuid) {
        wireMockServer.removeServeEvent(uuid);
    }

    /**
     * Removes the serve events for stubs matching metadata.
     *
     * @param stringValuePattern
     *            the string value pattern
     * @return the find serve events result
     */
    public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(StringValuePattern stringValuePattern) {
        return wireMockServer.removeServeEventsForStubsMatchingMetadata(stringValuePattern);
    }

    /**
     * Removes the serve events matching.
     *
     * @param requestPattern
     *            the request pattern
     * @return the find serve events result
     */
    public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
        return wireMockServer.removeServeEventsMatching(requestPattern);
    }

    /**
     * Removes the stub.
     *
     * @param mappingBuilder
     *            the mapping builder
     */
    public void removeStub(MappingBuilder mappingBuilder) {
        wireMockServer.removeStub(mappingBuilder);
    }

    /**
     * Removes the stub.
     *
     * @param stubMapping
     *            the stub mapping
     */
    public void removeStub(StubMapping stubMapping) {
        wireMockServer.removeStub(stubMapping);
    }

    /**
     * Removes the stub mapping.
     *
     * @param stubMapping
     *            the stub mapping
     */
    public void removeStubMapping(StubMapping stubMapping) {
        wireMockServer.removeStubMapping(stubMapping);
    }

    /**
     * Removes the stub mapping.
     *
     * @param id
     *            the id
     */
    public void removeStubMapping(UUID id) {
        wireMockServer.removeStubMapping(id);
    }

    /**
     * Removes the stub mappings by metadata.
     *
     * @param pattern
     *            the pattern
     */
    public void removeStubMappingsByMetadata(StringValuePattern pattern) {
        wireMockServer.removeStubMappingsByMetadata(pattern);
    }

    /**
     * Removes the stubs by metadata.
     *
     * @param pattern
     *            the pattern
     */
    public void removeStubsByMetadata(StringValuePattern pattern) {
        wireMockServer.removeStubsByMetadata(pattern);
    }

    /**
     * Reset all.
     */
    public void resetAll() {
        wireMockServer.resetAll();
    }

    /**
     * Reset mappings.
     */
    public void resetMappings() {
        wireMockServer.resetMappings();
    }

    /**
     * Reset requests.
     */
    public void resetRequests() {
        wireMockServer.resetRequests();
    }

    /**
     * Reset a scenario
     *
     * @param name
     *            the name
     */
    public void resetScenario(String name) {
        wireMockServer.resetScenario(name);
    }

    /**
     * Reset scenarios.
     */
    public void resetScenarios() {
        wireMockServer.resetScenarios();
    }

    /**
     * Reset to default mappings.
     */
    public void resetToDefaultMappings() {
        wireMockServer.resetToDefaultMappings();
    }

    /**
     * Save mappings.
     */
    public void saveMappings() {
        wireMockServer.saveMappings();
    }

    /**
     * Sets the global fixed delay.
     *
     * @param milliseconds
     *            the new global fixed delay
     */
    public void setGlobalFixedDelay(int milliseconds) {
        wireMockServer.setGlobalFixedDelay(milliseconds);
    }

    /**
     * Set scenario state
     *
     * @param name
     *            the name
     * @param state
     *            the state
     */
    public void setScenarioState(String name, String state) {
        wireMockServer.setScenarioState(name, state);
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        wireMockServer.shutdown();
    }

    /**
     * Shutdown server.
     */
    public void shutdownServer() {
        wireMockServer.shutdownServer();
    }

    /**
     * Snapshot record.
     *
     * @return the snapshot record result
     */
    public SnapshotRecordResult snapshotRecord() {
        return wireMockServer.snapshotRecord();
    }

    /**
     * Snapshot record.
     *
     * @param spec
     *            the spec
     * @return the snapshot record result
     */
    public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
        return wireMockServer.snapshotRecord(spec);
    }

    /**
     * Snapshot record.
     *
     * @param spec
     *            the spec
     * @return the snapshot record result
     */
    public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
        return wireMockServer.snapshotRecord(spec);
    }

    /**
     * Start.
     */
    public void start() {
        wireMockServer.start();
    }

    /**
     * Start recording.
     *
     * @param spec
     *            the spec
     */
    public void startRecording(RecordSpec spec) {
        wireMockServer.startRecording(spec);
    }

    /**
     * Start recording.
     *
     * @param recordSpec
     *            the record spec
     */
    public void startRecording(RecordSpecBuilder recordSpec) {
        wireMockServer.startRecording(recordSpec);
    }

    /**
     * Start recording.
     *
     * @param targetBaseUrl
     *            the target base url
     */
    public void startRecording(String targetBaseUrl) {
        wireMockServer.startRecording(targetBaseUrl);
    }

    /**
     * Stop.
     */
    public void stop() {
        wireMockServer.stop();
    }

    /**
     * Stop recording.
     *
     * @return the snapshot record result
     */
    public SnapshotRecordResult stopRecording() {
        return wireMockServer.stopRecording();
    }

    /**
     * Stub for.
     *
     * @param mappingBuilder
     *            the mapping builder
     * @return the stub mapping
     */
    public StubMapping stubFor(MappingBuilder mappingBuilder) {
        return wireMockServer.stubFor(mappingBuilder);
    }

    /**
     * Update global settings.
     *
     * @param newSettings
     *            the new settings
     */
    public void updateGlobalSettings(GlobalSettings newSettings) {
        wireMockServer.updateGlobalSettings(newSettings);
    }

    /**
     * Url.
     *
     * @param path
     *            the path
     * @return the string
     */
    public String url(String path) {
        return wireMockServer.url(path);
    }

    /**
     * Verify.
     *
     * @param countMatchingStrategy
     *            the count matching strategy
     * @param requestPatternBuilder
     *            the request pattern builder
     */
    public void verify(CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder) {
        wireMockServer.verify(countMatchingStrategy, requestPatternBuilder);
    }

    /**
     * Verify.
     *
     * @param requestPatternBuilder
     *            the request pattern builder
     */
    public void verify(RequestPatternBuilder requestPatternBuilder) {
        wireMockServer.verify(requestPatternBuilder);
    }

    /**
     * Verify.
     *
     * @param count
     *            the count
     * @param requestPatternBuilder
     *            the request pattern builder
     */
    public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
        wireMockServer.verify(count, requestPatternBuilder);
    }

    private Statement apply(final Statement base, final String methodName) {
        return new Statement() {
            public void evaluate() throws Throwable {
                WireMockRule.this.methodName = methodName;
                final Options localOptions = new WireMockRuleConfiguration(WireMockRule.this.options, methodName);

                new File(localOptions.filesRoot().getPath(), "mappings").mkdirs();
                new File(localOptions.filesRoot().getPath(), "__files").mkdirs();

                WireMockRule.this.wireMockServer = new WireMockServer(localOptions);
                WireMockRule.this.start();
                WireMock.configureFor("localhost", WireMockRule.this.port());

                try {
                    WireMockRule.this.before();
                    base.evaluate();
                    WireMockRule.this.checkForUnmatchedRequests();
                } finally {
                    WireMockRule.this.after();
                    WireMockRule.this.stop();
                    WireMockRule.this.methodName = null;
                }

            }
        };
    }

    private void checkForUnmatchedRequests() {
        if (this.failOnUnmatchedRequests) {
            List<LoggedRequest> unmatchedRequests = this.findAllUnmatchedRequests();
            if (!unmatchedRequests.isEmpty()) {
                List<NearMiss> nearMisses = this.findNearMissesForAllUnmatchedRequests();
                if (nearMisses.isEmpty()) {
                    throw VerificationException.forUnmatchedRequests(unmatchedRequests);
                }

                throw VerificationException.forUnmatchedNearMisses(nearMisses);
            }
        }

    }

    /**
     * After.
     */
    protected void after() {
    }

    /**
     * Before.
     */
    protected void before() {
    }
}

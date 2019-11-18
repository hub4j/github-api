package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.*;
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
 * @author Liam Newman
 */
public class WireMockRule implements MethodRule, TestRule, Container, Stubbing, Admin {

    private WireMockServer wireMockServer;
    private boolean failOnUnmatchedRequests;
    private final Options options;

    public String getMethodName() {
        return methodName;
    }

    private String methodName = null;

    public WireMockRule(Options options) {
        this(options, true);
    }

    public WireMockRule(Options options, boolean failOnUnmatchedRequests) {
        this.options = options;
        this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    }

    public WireMockRule(int port) {
        this(WireMockConfiguration.wireMockConfig().port(port));
    }

    public WireMockRule(int port, Integer httpsPort) {
        this(WireMockConfiguration.wireMockConfig().port(port).httpsPort(httpsPort));
    }

    public WireMockRule() {
        this(WireMockRuleConfiguration.wireMockConfig());
    }

    public Statement apply(Statement base, Description description) {
        return this.apply(base, description.getMethodName());
    }

    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return this.apply(base, method.getName());
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

    protected void before() {
    }

    protected void after() {
    }

    public void loadMappingsUsing(MappingsLoader mappingsLoader) {
        wireMockServer.loadMappingsUsing(mappingsLoader);
    }

    public GlobalSettingsHolder getGlobalSettingsHolder() {
        return wireMockServer.getGlobalSettingsHolder();
    }

    public void addMockServiceRequestListener(RequestListener listener) {
        wireMockServer.addMockServiceRequestListener(listener);
    }

    public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
        wireMockServer.enableRecordMappings(mappingsFileSource, filesFileSource);
    }

    public void stop() {
        wireMockServer.stop();
    }

    public void start() {
        wireMockServer.start();
    }

    public void shutdown() {
        wireMockServer.shutdown();
    }

    public int port() {
        return wireMockServer.port();
    }

    public int httpsPort() {
        return wireMockServer.httpsPort();
    }

    public String url(String path) {
        return wireMockServer.url(path);
    }

    public String baseUrl() {
        return wireMockServer.baseUrl();
    }

    public boolean isRunning() {
        return wireMockServer.isRunning();
    }

    public StubMapping givenThat(MappingBuilder mappingBuilder) {
        return wireMockServer.givenThat(mappingBuilder);
    }

    public StubMapping stubFor(MappingBuilder mappingBuilder) {
        return wireMockServer.stubFor(mappingBuilder);
    }

    public void editStub(MappingBuilder mappingBuilder) {
        wireMockServer.editStub(mappingBuilder);
    }

    public void removeStub(MappingBuilder mappingBuilder) {
        wireMockServer.removeStub(mappingBuilder);
    }

    public void removeStub(StubMapping stubMapping) {
        wireMockServer.removeStub(stubMapping);
    }

    public List<StubMapping> getStubMappings() {
        return wireMockServer.getStubMappings();
    }

    public StubMapping getSingleStubMapping(UUID id) {
        return wireMockServer.getSingleStubMapping(id);
    }

    public List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern) {
        return wireMockServer.findStubMappingsByMetadata(pattern);
    }

    public void removeStubMappingsByMetadata(StringValuePattern pattern) {
        wireMockServer.removeStubMappingsByMetadata(pattern);
    }

    public void removeStubMapping(StubMapping stubMapping) {
        wireMockServer.removeStubMapping(stubMapping);
    }

    public void verify(RequestPatternBuilder requestPatternBuilder) {
        wireMockServer.verify(requestPatternBuilder);
    }

    public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
        wireMockServer.verify(count, requestPatternBuilder);
    }

    public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return wireMockServer.findAll(requestPatternBuilder);
    }

    public List<ServeEvent> getAllServeEvents() {
        return wireMockServer.getAllServeEvents();
    }

    public void setGlobalFixedDelay(int milliseconds) {
        wireMockServer.setGlobalFixedDelay(milliseconds);
    }

    public List<LoggedRequest> findAllUnmatchedRequests() {
        return wireMockServer.findAllUnmatchedRequests();
    }

    public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
        return wireMockServer.findNearMissesForAllUnmatchedRequests();
    }

    public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
        return wireMockServer.findAllNearMissesFor(requestPatternBuilder);
    }

    public List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
        return wireMockServer.findNearMissesFor(loggedRequest);
    }

    public void addStubMapping(StubMapping stubMapping) {
        wireMockServer.addStubMapping(stubMapping);
    }

    public void editStubMapping(StubMapping stubMapping) {
        wireMockServer.editStubMapping(stubMapping);
    }

    public ListStubMappingsResult listAllStubMappings() {
        return wireMockServer.listAllStubMappings();
    }

    public SingleStubMappingResult getStubMapping(UUID id) {
        return wireMockServer.getStubMapping(id);
    }

    public void saveMappings() {
        wireMockServer.saveMappings();
    }

    public void resetAll() {
        wireMockServer.resetAll();
    }

    public void resetRequests() {
        wireMockServer.resetRequests();
    }

    public void resetToDefaultMappings() {
        wireMockServer.resetToDefaultMappings();
    }

    public GetServeEventsResult getServeEvents() {
        return wireMockServer.getServeEvents();
    }

    public SingleServedStubResult getServedStub(UUID id) {
        return wireMockServer.getServedStub(id);
    }

    public void resetScenarios() {
        wireMockServer.resetScenarios();
    }

    public void resetMappings() {
        wireMockServer.resetMappings();
    }

    public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
        return wireMockServer.countRequestsMatching(requestPattern);
    }

    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        return wireMockServer.findRequestsMatching(requestPattern);
    }

    public FindRequestsResult findUnmatchedRequests() {
        return wireMockServer.findUnmatchedRequests();
    }

    public void removeServeEvent(UUID uuid) {
        wireMockServer.removeServeEvent(uuid);
    }

    public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
        return wireMockServer.removeServeEventsMatching(requestPattern);
    }

    public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(StringValuePattern stringValuePattern) {
        return wireMockServer.removeServeEventsForStubsMatchingMetadata(stringValuePattern);
    }

    public void updateGlobalSettings(GlobalSettings newSettings) {
        wireMockServer.updateGlobalSettings(newSettings);
    }

    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
        return wireMockServer.findNearMissesForUnmatchedRequests();
    }

    public GetScenariosResult getAllScenarios() {
        return wireMockServer.getAllScenarios();
    }

    public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
        return wireMockServer.findTopNearMissesFor(loggedRequest);
    }

    public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
        return wireMockServer.findTopNearMissesFor(requestPattern);
    }

    public void startRecording(String targetBaseUrl) {
        wireMockServer.startRecording(targetBaseUrl);
    }

    public void startRecording(RecordSpec spec) {
        wireMockServer.startRecording(spec);
    }

    public void startRecording(RecordSpecBuilder recordSpec) {
        wireMockServer.startRecording(recordSpec);
    }

    public SnapshotRecordResult stopRecording() {
        return wireMockServer.stopRecording();
    }

    public RecordingStatusResult getRecordingStatus() {
        return wireMockServer.getRecordingStatus();
    }

    public SnapshotRecordResult snapshotRecord() {
        return wireMockServer.snapshotRecord();
    }

    public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
        return wireMockServer.snapshotRecord(spec);
    }

    public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
        return wireMockServer.snapshotRecord(spec);
    }

    public Options getOptions() {
        return wireMockServer.getOptions();
    }

    public void shutdownServer() {
        wireMockServer.shutdownServer();
    }

    public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
        return wireMockServer.findAllStubsByMetadata(pattern);
    }

    public void removeStubsByMetadata(StringValuePattern pattern) {
        wireMockServer.removeStubsByMetadata(pattern);
    }

    public void importStubs(StubImport stubImport) {
        wireMockServer.importStubs(stubImport);
    }

    public GetGlobalSettingsResult getGlobalSettings() {
        return wireMockServer.getGlobalSettings();
    }
}

package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The standard WireMockRule eagerly initializes a WireMockServer. This version supports multiple servers in one rule
 * and takes a lazy approach to intitialization allowing us to isolate files snapshots for each method.
 *
 * @author Liam Newman
 */
public class WireMockMultiServerRule implements MethodRule, TestRule {

    protected final Map<String, WireMockServer> servers = new HashMap<>();
    private boolean failOnUnmatchedRequests;
    private final Options options;

    public String getMethodName() {
        return methodName;
    }

    private String methodName = null;

    public WireMockMultiServerRule(Options options) {
        this(options, true);
    }

    public WireMockMultiServerRule(Options options, boolean failOnUnmatchedRequests) {
        this.options = options;
        this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    }

    public WireMockMultiServerRule() {
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
                WireMockMultiServerRule.this.methodName = methodName;
                initializeServers();
                WireMock.configureFor("localhost", WireMockMultiServerRule.this.servers.get("default").port());

                try {
                    WireMockMultiServerRule.this.before();
                    base.evaluate();
                    WireMockMultiServerRule.this.checkForUnmatchedRequests();
                } finally {
                    WireMockMultiServerRule.this.after();
                    WireMockMultiServerRule.this.stop();
                    WireMockMultiServerRule.this.methodName = null;
                    WireMockMultiServerRule.this.servers.clear();
                }

            }
        };
    }

    protected void initializeServers() {
    }

    protected final void initializeServer(String serverId, Extension... extensions) {
        String directoryName = methodName;
        if (!serverId.equals("default")) {
            directoryName += "_" + serverId;
        }

        final Options localOptions = new WireMockRuleConfiguration(WireMockMultiServerRule.this.options, directoryName,
                extensions);

        new File(localOptions.filesRoot().getPath(), "mappings").mkdirs();
        new File(localOptions.filesRoot().getPath(), "__files").mkdirs();

        WireMockServer server = new WireMockServer(localOptions);
        this.servers.put(serverId, server);
        server.start();

        if (!serverId.equals("default")) {
            WireMock.configureFor("localhost", server.port());
        }
    }

    protected void before() {
    }

    protected void after() {
    }

    private void checkForUnmatchedRequests() {
        servers.values().forEach(server -> checkForUnmatchedRequests(server));
    }

    private void checkForUnmatchedRequests(WireMockServer server) {
        if (this.failOnUnmatchedRequests) {
            List<LoggedRequest> unmatchedRequests = server.findAllUnmatchedRequests();
            if (!unmatchedRequests.isEmpty()) {
                List<NearMiss> nearMisses = server.findNearMissesForAllUnmatchedRequests();
                if (nearMisses.isEmpty()) {
                    throw VerificationException.forUnmatchedRequests(unmatchedRequests);
                }

                throw VerificationException.forUnmatchedNearMisses(nearMisses);
            }
        }

    }

    private boolean deleteEmptyFolders(File path) {
        boolean deleteable = path.isDirectory();
        if (deleteable) {
            for (File file : path.listFiles()) {
                // if at any point in tree we find something we can't delete
                // we don't need to keep
                if (!deleteEmptyFolders(file)) {
                    deleteable = false;
                }
            }

            if (deleteable) {
                deleteable = deleteable && path.delete();
            }
        }
        return deleteable;

    }

    private void stop() {
        servers.values().forEach(server -> {
            server.stop();
            // server left behinds empty folders delete them
            deleteEmptyFolders(new File(server.getOptions().filesRoot().getPath()));
        });
    }

}

package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHRepositoryRule.AlertsThreshold;
import org.kohsuke.github.GHRepositoryRule.CodeScanningTool;
import org.kohsuke.github.GHRepositoryRule.Operator;
import org.kohsuke.github.GHRepositoryRule.Parameter;
import org.kohsuke.github.GHRepositoryRule.Parameters;
import org.kohsuke.github.GHRepositoryRule.SecurityAlertsThreshold;
import org.kohsuke.github.GHRepositoryRule.StatusCheckConfiguration;
import org.kohsuke.github.GHRepositoryRule.StringParameter;
import org.kohsuke.github.GHRepositoryRule.WorkflowFileReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test class for GHRepositoryRule.
 */
public class GHRepositoryRuleTest {

    /**
     * Create default GHRepositoryRuleTest instance
     */
    public GHRepositoryRuleTest() {
    }

    /**
     * Tests to cover AlertsThreshold enum.
     */
    @Test
    public void testAlertsThreshold() {
        assertThat(AlertsThreshold.ERRORS, is(notNullValue()));
    }

    /**
     * Tests to cover CodeScanningTool class.
     */
    @Test
    public void testCodeScanningTool() {
        CodeScanningTool codeScanningTool = new CodeScanningTool();
        codeScanningTool = new CodeScanningTool();
        assertThat(codeScanningTool.getAlertsThreshold(), is(nullValue()));
        assertThat(codeScanningTool.getSecurityAlertsThreshold(), is(nullValue()));
        assertThat(codeScanningTool.getTool(), is(nullValue()));
    }

    /**
     * Tests to cover Operator enum.
     */
    @Test
    public void testOperator() {
        assertThat(Operator.ENDS_WITH, is(notNullValue()));
    }

    /**
     * Tests that apply on null JsonNode returns null.
     *
     * @throws Exception
     *             if something goes wrong.
     */
    @Test
    public void testParameterReturnsNullOnNullArg() throws Exception {
        Parameter<String> parameter = new StringParameter("any");
        assertThat(parameter.apply(null, null), is(nullValue()));
    }

    /**
     * Test to cover the constructor of the Parameters class.
     */
    @Test
    public void testParameters() {
        assertThat(Parameters.REQUIRED_DEPLOYMENT_ENVIRONMENTS.getType(), is(notNullValue()));
        assertThat(Parameters.REQUIRED_STATUS_CHECKS.getType(), is(notNullValue()));
        assertThat(Parameters.OPERATOR.getType(), is(notNullValue()));
        assertThat(Parameters.WORKFLOWS.getType(), is(notNullValue()));
        assertThat(Parameters.CODE_SCANNING_TOOLS.getType(), is(notNullValue()));
        assertThat(new StringParameter("any").getType(), is(notNullValue()));
    }

    /**
     * Tests to cover SecurityAlertsThreshold enum.
     */
    @Test
    public void testSecurityAlertsThreshold() {
        assertThat(SecurityAlertsThreshold.HIGH_OR_HIGHER, is(notNullValue()));
    }

    /**
     * Tests to cover StatusCheckConfiguration class.
     */
    @Test
    public void testStatusCheckConfiguration() {
        StatusCheckConfiguration statusCheckConfiguration = new StatusCheckConfiguration();
        statusCheckConfiguration = new StatusCheckConfiguration();
        assertThat(statusCheckConfiguration.getContext(), is(nullValue()));
        assertThat(statusCheckConfiguration.getIntegrationId(), is(nullValue()));
    }

    /**
     * Tests to cover WorkflowFileReference class.
     */
    @Test
    public void testWorkflowFileReference() {
        WorkflowFileReference workflowFileReference = new WorkflowFileReference();
        assertThat(workflowFileReference.getPath(), is(nullValue()));
        assertThat(workflowFileReference.getRef(), is(nullValue()));
        assertThat(workflowFileReference.getRepositoryId(), is(equalTo(0L)));
        assertThat(workflowFileReference.getSha(), is(nullValue()));
    }
}

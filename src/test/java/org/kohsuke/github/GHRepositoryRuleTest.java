package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHRepositoryRule.AlertsThreshold;
import org.kohsuke.github.GHRepositoryRule.CodeScanningTool;
import org.kohsuke.github.GHRepositoryRule.Operator;
import org.kohsuke.github.GHRepositoryRule.Parameters;
import org.kohsuke.github.GHRepositoryRule.SecurityAlertsThreshold;
import org.kohsuke.github.GHRepositoryRule.StatusCheckConfiguration;
import org.kohsuke.github.GHRepositoryRule.StringParameter;
import org.kohsuke.github.GHRepositoryRule.WorkflowFileReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test class for GHRepositoryRule.
 */
public class GHRepositoryRuleTest {
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
     * Tests to cover StatusCheckConfiguration class.
     */
    @Test
    public void testStatusCheckConfiguration() {
        StatusCheckConfiguration statusCheckConfiguration = new StatusCheckConfiguration();
        statusCheckConfiguration = new StatusCheckConfiguration("context", 3);
        assertThat(statusCheckConfiguration.getContext(), is(equalTo("context")));
        assertThat(statusCheckConfiguration.getIntegrationId(), is(equalTo(3)));
    }

    /**
     * Tests to cover WorkflowFileReference class.
     */
    @Test
    public void testWorkflowFileReference() {
        WorkflowFileReference workflowFileReference = new WorkflowFileReference();
        workflowFileReference = new WorkflowFileReference("path", "ref", 13, "sha");
        assertThat(workflowFileReference.getPath(), is(equalTo("path")));
        assertThat(workflowFileReference.getRef(), is(equalTo("ref")));
        assertThat(workflowFileReference.getRepositoryId(), is(equalTo(13)));
        assertThat(workflowFileReference.getSha(), is(equalTo("sha")));
    }

    /**
     * Tests to cover CodeScanningTool class.
     */
    @Test
    public void testCodeScanningTool() {
        CodeScanningTool codeScanningTool = new CodeScanningTool();
        codeScanningTool = new CodeScanningTool(AlertsThreshold.ERRORS, SecurityAlertsThreshold.HIGH_OR_HIGHER, "tool");
        assertThat(codeScanningTool.getAlertsThreshold(), is(equalTo(AlertsThreshold.ERRORS)));
        assertThat(codeScanningTool.getSecurityAlertsThreshold(), is(equalTo(SecurityAlertsThreshold.HIGH_OR_HIGHER)));
        assertThat(codeScanningTool.getTool(), is(equalTo("tool")));
    }

    /**
     * Tests to cover Operator enum.
     */
    @Test
    public void testOperator() {
        assertThat(Operator.ENDS_WITH, is(notNullValue()));
    }
}

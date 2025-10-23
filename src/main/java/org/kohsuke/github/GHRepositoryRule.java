package org.kohsuke.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a repository rule.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHRepositoryRule extends GitHubInteractiveObject {

    /**
     * Alerts threshold parameter.
     */
    public static enum AlertsThreshold {
        /**
         * all
         */
        ALL,

        /**
         * errors
         */
        ERRORS,

        /**
         * errors_and_warnings
         */
        ERRORS_AND_WARNINGS,

        /**
         * none
         */
        NONE
    }

    /**
     * Boolean parameter for a ruleset.
     */
    public static class BooleanParameter extends Parameter<Boolean> {
        /**
         * Instantiates a new boolean parameter.
         *
         * @param key
         *            the key
         */
        public BooleanParameter(String key) {
            super(key);
        }

        @Override
        TypeReference<Boolean> getType() {
            return new TypeReference<Boolean>() {
            };
        }
    }
    /**
     * Code scanning tool parameter.
     */
    public static class CodeScanningTool {

        private AlertsThreshold alertsThreshold;

        private SecurityAlertsThreshold securityAlertsThreshold;
        private String tool;
        /**
         * Create default CodeScanningTool instance
         */
        public CodeScanningTool() {
        }

        /**
         * Gets the alerts threshold.
         *
         * @return the alerts threshold
         */
        public AlertsThreshold getAlertsThreshold() {
            return this.alertsThreshold;
        }

        /**
         * Gets the security alerts threshold.
         *
         * @return the security alerts threshold
         */
        public SecurityAlertsThreshold getSecurityAlertsThreshold() {
            return this.securityAlertsThreshold;
        }

        /**
         * Gets the tool.
         *
         * @return the tool
         */
        public String getTool() {
            return this.tool;
        }
    }
    /**
     * Integer parameter for a ruleset.
     */
    public static class IntegerParameter extends Parameter<Integer> {
        /**
         * Instantiates a new integer parameter.
         *
         * @param key
         *            the key
         */
        public IntegerParameter(String key) {
            super(key);
        }

        @Override
        TypeReference<Integer> getType() {
            return new TypeReference<Integer>() {
            };
        }
    }
    /**
     * List parameter for a ruleset.
     *
     * @param <T>
     *            the type of the list
     */
    public abstract static class ListParameter<T> extends Parameter<List<T>> {
        /**
         * Instantiates a new list parameter.
         *
         * @param key
         *            the key
         */
        public ListParameter(String key) {
            super(key);
        }
    }
    /**
     * Operator parameter.
     */
    public static enum Operator {
        /**
         * contains
         */
        CONTAINS,

        /**
         * ends_with
         */
        ENDS_WITH,

        /**
         * regex
         */
        REGEX,

        /**
         * starts_with
         */
        STARTS_WITH
    }

    /**
     * Basic parameter for a ruleset.
     *
     * @param <T>
     *            the type of the parameter
     */
    public abstract static class Parameter<T> {

        private final String key;

        /**
         * Instantiates a new parameter.
         *
         * @param key
         *            the key
         */
        protected Parameter(String key) {
            this.key = key;
        }

        T apply(JsonNode jsonNode, GitHub root) throws IOException {
            if (jsonNode == null) {
                return null;
            }
            return GitHubClient.getMappingObjectReader(root).forType(this.getType()).readValue(jsonNode);
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        String getKey() {
            return this.key;
        }

        /**
         * Get the parameter type reference for type mapping.
         */
        abstract TypeReference<T> getType();
    }

    /**
     * Available parameters for a ruleset.
     */
    public interface Parameters {
        /**
         * code_scanning_tools parameter
         */
        public static final ListParameter<CodeScanningTool> CODE_SCANNING_TOOLS = new ListParameter<CodeScanningTool>(
                "code_scanning_tools") {
            @Override
            TypeReference<List<CodeScanningTool>> getType() {
                return new TypeReference<List<CodeScanningTool>>() {
                };
            }
        };
        /**
         * dismiss_stale_reviews_on_push parameter
         */
        public static final BooleanParameter DISMISS_STALE_REVIEWS_ON_PUSH = new BooleanParameter(
                "dismiss_stale_reviews_on_push");
        /**
         * name parameter
         */
        public static final StringParameter NAME = new StringParameter("name");
        /**
         * negate parameter
         */
        public static final BooleanParameter NEGATE = new BooleanParameter("negate");
        /**
         * operator parameter
         */
        public static final Parameter<Operator> OPERATOR = new Parameter<Operator>("operator") {
            @Override
            TypeReference<Operator> getType() {
                return new TypeReference<Operator>() {
                };
            }
        };
        /**
         * regex parameter
         */
        public static final StringParameter REGEX = new StringParameter("regex");
        /**
         * required_approving_review_count parameter
         */
        public static final IntegerParameter REQUIRED_APPROVING_REVIEW_COUNT = new IntegerParameter(
                "required_approving_review_count");
        /**
         * required_deployment_environments parameter
         */
        public static final ListParameter<String> REQUIRED_DEPLOYMENT_ENVIRONMENTS = new ListParameter<String>(
                "required_deployment_environments") {
            @Override
            TypeReference<List<String>> getType() {
                return new TypeReference<List<String>>() {
                };
            }
        };
        /**
         * required_review_thread_resolution parameter
         */
        public static final BooleanParameter REQUIRED_REVIEW_THREAD_RESOLUTION = new BooleanParameter(
                "required_review_thread_resolution");
        /**
         * required_status_checks parameter
         */
        public static final ListParameter<StatusCheckConfiguration> REQUIRED_STATUS_CHECKS = new ListParameter<StatusCheckConfiguration>(
                "required_status_checks") {
            @Override
            TypeReference<List<StatusCheckConfiguration>> getType() {
                return new TypeReference<List<StatusCheckConfiguration>>() {
                };
            }
        };
        /**
         * require_code_owner_review parameter
         */
        public static final BooleanParameter REQUIRE_CODE_OWNER_REVIEW = new BooleanParameter(
                "require_code_owner_review");
        /**
         * require_last_push_approval parameter
         */
        public static final BooleanParameter REQUIRE_LAST_PUSH_APPROVAL = new BooleanParameter(
                "require_last_push_approval");
        /**
         * strict_required_status_checks_policy parameter
         */
        public static final BooleanParameter STRICT_REQUIRED_STATUS_CHECKS_POLICY = new BooleanParameter(
                "strict_required_status_checks_policy");
        /**
         * update_allows_fetch_and_merge parameter
         */
        public static final BooleanParameter UPDATE_ALLOWS_FETCH_AND_MERGE = new BooleanParameter(
                "update_allows_fetch_and_merge");
        /**
         * workflows parameter
         */
        public static final ListParameter<WorkflowFileReference> WORKFLOWS = new ListParameter<WorkflowFileReference>(
                "workflows") {
            @Override
            TypeReference<List<WorkflowFileReference>> getType() {
                return new TypeReference<List<WorkflowFileReference>>() {
                };
            }
        };
    }

    /**
     * The source of the ruleset type.
     */
    public enum RulesetSourceType {
        /**
         * Organization
         */
        ORGANIZATION,

        /**
         * Repository
         */
        REPOSITORY,

        /**
         * unknown
         */
        UNKNOWN
    }

    /**
     * Security alerts threshold parameter.
     */
    public static enum SecurityAlertsThreshold {
        /**
         * all
         */
        ALL,

        /**
         * critical
         */
        CRITICAL,

        /**
         * high_or_higher
         */
        HIGH_OR_HIGHER,

        /**
         * medium_or_higher
         */
        MEDIUM_OR_HIGHER,

        /**
         * none
         */
        NONE
    }

    /**
     * Status check configuration parameter.
     */
    public static class StatusCheckConfiguration {

        private String context;

        private Integer integrationId;
        /**
         * Create default StatusCheckConfiguration instance
         */
        public StatusCheckConfiguration() {
        }

        /**
         * Gets the context.
         *
         * @return the context
         */
        public String getContext() {
            return this.context;
        }

        /**
         * Gets the integration id.
         *
         * @return the integration id
         */
        public Integer getIntegrationId() {
            return this.integrationId;
        }
    }

    /**
     * String parameter for a ruleset.
     */
    public static class StringParameter extends Parameter<String> {
        /**
         * Instantiates a new string parameter.
         *
         * @param key
         *            the key
         */
        public StringParameter(String key) {
            super(key);
        }

        @Override
        TypeReference<String> getType() {
            return new TypeReference<String>() {
            };
        }
    }

    /**
     * The type of the ruleset.
     */
    public static enum Type {
        /**
         * branch_name_pattern
         */
        BRANCH_NAME_PATTERN,

        /**
         * code_scanning
         */
        CODE_SCANNING,

        /**
         * committer_email_pattern
         */
        COMMITTER_EMAIL_PATTERN,

        /**
         * commit_author_email_pattern
         */
        COMMIT_AUTHOR_EMAIL_PATTERN,

        /**
         * commit_message_pattern
         */
        COMMIT_MESSAGE_PATTERN,

        /**
         * creation
         */
        CREATION,

        /**
         * deletion
         */
        DELETION,

        /**
         * non_fast_forward
         */
        NON_FAST_FORWARD,

        /**
         * pull_request
         */
        PULL_REQUEST,

        /**
         * required_deployments
         */
        REQUIRED_DEPLOYMENTS,

        /**
         * required_linear_history
         */
        REQUIRED_LINEAR_HISTORY,

        /**
         * required_signatures
         */
        REQUIRED_SIGNATURES,

        /**
         * required_status_checks
         */
        REQUIRED_STATUS_CHECKS,

        /**
         * tag_name_pattern
         */
        TAG_NAME_PATTERN,

        /**
         * unknown
         */
        UNKNOWN,

        /**
         * update
         */
        UPDATE,

        /**
         * workflows
         */
        WORKFLOWS
    }

    /**
     * Workflow file reference parameter.
     */
    public static class WorkflowFileReference {

        private String path;

        private String ref;
        private long repositoryId;
        private String sha;
        /**
         * Create default WorkflowFileReference instance
         */
        public WorkflowFileReference() {
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public String getPath() {
            return this.path;
        }

        /**
         * Gets the ref.
         *
         * @return the ref
         */
        public String getRef() {
            return this.ref;
        }

        /**
         * Gets the repository id.
         *
         * @return the repository id
         */
        public long getRepositoryId() {
            return this.repositoryId;
        }

        /**
         * Gets the sha.
         *
         * @return the sha
         */
        public String getSha() {
            return this.sha;
        }
    }

    private Map<String, JsonNode> parameters;

    private long rulesetId;

    private String rulesetSource;

    private String rulesetSourceType;

    private String type;

    /**
     * Create default GHRepositoryRule instance
     */
    public GHRepositoryRule() {
    }

    /**
     * Gets a parameter. ({@link GHRepositoryRule.Parameters Parameters} provides a list of available parameters.)
     *
     * @param parameter
     *            the parameter
     * @param <T>
     *            the type of the parameter
     * @return the parameters
     * @throws IOException
     *             if an I/O error occurs
     */
    public <T> Optional<T> getParameter(Parameter<T> parameter) throws IOException {
        if (this.parameters == null) {
            return Optional.empty();
        }
        JsonNode jsonNode = this.parameters.get(parameter.getKey());
        if (jsonNode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(parameter.apply(jsonNode, root()));
    }

    /**
     * Gets the ruleset id.
     *
     * @return the ruleset id
     */
    public long getRulesetId() {
        return this.rulesetId;
    }

    /**
     * Gets the ruleset source.
     *
     * @return the ruleset source
     */
    public String getRulesetSource() {
        return this.rulesetSource;
    }

    /**
     * Gets the ruleset source type.
     *
     * @return the ruleset source type
     */
    public RulesetSourceType getRulesetSourceType() {
        return EnumUtils.getEnumOrDefault(RulesetSourceType.class, this.rulesetSourceType, RulesetSourceType.UNKNOWN);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType() {
        return EnumUtils.getEnumOrDefault(Type.class, this.type, Type.UNKNOWN);
    }
}

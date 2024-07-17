package org.kohsuke.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a repository rule.
 */
public class GHRepositoryRule {
    private Type type;
    private RulesetSourceType rulesetSourceType;
    private String rulesetSource;
    private long rulesetId;
    private Map<String, JsonNode> parameters;

    /**
     * Instantiates a new GH repository rule.
     */
    public GHRepositoryRule() {
    }

    /**
     * Instantiates a new GH repository rule.
     *
     * @param type
     *            the type
     * @param ruleset_type_source
     *            the ruleset type source
     * @param ruleset_source
     *            the ruleset source
     * @param ruleset_id
     *            the ruleset id
     * @param parameters
     *            the parameters
     */
    public GHRepositoryRule(Type type,
            RulesetSourceType ruleset_type_source,
            String ruleset_source,
            long ruleset_id,
            Map<String, JsonNode> parameters) {
        this.type = type;
        this.ruleset_source_type = ruleset_type_source;
        this.ruleset_source = ruleset_source;
        this.ruleset_id = ruleset_id;
        this.parameters = new HashMap<>(parameters);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Gets the ruleset source type.
     *
     * @return the ruleset source type
     */
    public RulesetSourceType getRulesetSourceType() {
        return this.ruleset_source_type;
    }

    /**
     * Gets the ruleset source.
     *
     * @return the ruleset source
     */
    public String getRulesetSource() {
        return this.ruleset_source;
    }

    /**
     * Gets the ruleset id.
     *
     * @return the ruleset id
     */
    public long getRulesetId() {
        return this.ruleset_id;
    }

    /**
     * Gets a parameter. ({@link GHRepositoryRule.Parameters Parameters} provides a list of available parameters.)
     *
     * @param parameter
     *            the parameter
     * @param <T>
     *            the type of the parameter
     * @return the parameters
     */
    public <T> Optional<T> getParameter(Parameter<T> parameter) {
        return Optional.ofNullable(this.parameters).map(p -> p.get(parameter.getKey())).map(parameter);
    }

    /**
     * The type of the ruleset.
     */
    public static enum Type {
        /**
         * creation
         */
        creation,

        /**
         * update
         */
        update,

        /**
         * deletion
         */
        deletion,

        /**
         * required_linear_history
         */
        required_linear_history,

        /**
         * required_deployments
         */
        required_deployments,

        /**
         * required_signatures
         */
        required_signatures,

        /**
         * pull_request
         */
        pull_request,

        /**
         * required_status_checks
         */
        required_status_checks,

        /**
         * non_fast_forward
         */
        non_fast_forward,

        /**
         * commit_message_pattern
         */
        commit_message_pattern,

        /**
         * commit_author_email_pattern
         */
        commit_author_email_pattern,

        /**
         * committer_email_pattern
         */
        committer_email_pattern,

        /**
         * branch_name_pattern
         */
        branch_name_pattern,

        /**
         * tag_name_pattern
         */
        tag_name_pattern,

        /**
         * workflows
         */
        workflows,

        /**
         * code_scanning
         */
        code_scanning
    }

    /**
     * The source of the ruleset type.
     */
    public enum RulesetSourceType {
        /**
         * Repository
         */
        Repository,

        /**
         * Organization
         */
        Organization
    }

    /**
     * Available parameters for a ruleset.
     */
    public interface Parameters {
        /**
         * update_allows_fetch_and_merge paramter
         */
        public static final BooleanParameter UPDATE_ALLOWS_FETCH_AND_MERGE = new BooleanParameter(
                "update_allows_fetch_and_merge");
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
         * dismiss_stale_reviews_on_push parameter
         */
        public static final BooleanParameter DISMISS_STALE_REVIEWS_ON_PUSH = new BooleanParameter(
                "dismiss_stale_reviews_on_push");
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
         * required_approving_review_count parameter
         */
        public static final IntegerParameter REQUIRED_APPROVING_REVIEW_COUNT = new IntegerParameter(
                "required_approving_review_count");
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
         * strict_required_status_checks_policy parameter
         */
        public static final BooleanParameter STRICT_REQUIRED_STATUS_CHECKS_POLICY = new BooleanParameter(
                "strict_required_status_checks_policy");
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
    }

    /**
     * Basic parameter for a ruleset.
     *
     * @param <T>
     *            the type of the parameter
     */
    public abstract static class Parameter<T> implements Function<JsonNode, T> {

        private final static ObjectMapper objectMapper = new ObjectMapper();

        private final String key;

        /**
         * Get the parameter type reference for type mapping.
         */
        abstract TypeReference<T> getType();

        /**
         * Instantiates a new parameter.
         *
         * @param key
         *            the key
         */
        protected Parameter(String key) {
            this.key = key;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        String getKey() {
            return this.key;
        }

        @Override
        public T apply(JsonNode jsonNode) {
            if (jsonNode == null) {
                return null;
            }
            return objectMapper.convertValue(jsonNode, getType());
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
     * Status check configuration parameter.
     */
    public static class StatusCheckConfiguration {
        private String context;
        private Integer integration_id;

        /**
         * Instantiates a new status check configuration.
         */
        public StatusCheckConfiguration() {
        }

        /**
         * Instantiates a new status check configuration.
         *
         * @param context
         *            the context
         * @param integration_id
         *            the integration id
         */
        public StatusCheckConfiguration(String context, Integer integration_id) {
            this.context = context;
            this.integration_id = integration_id;
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
            return this.integration_id;
        }
    }

    /**
     * Operator parameter.
     */
    public static enum Operator {
        /**
         * starts_with
         */
        starts_with,

        /**
         * ends_with
         */
        ends_with,

        /**
         * contains
         */
        contains,

        /**
         * regex
         */
        regex
    }

    /**
     * Workflow file reference parameter.
     */
    public static class WorkflowFileReference {
        private String path;
        private String ref;
        private int repository_id;
        private String sha;

        /**
         * Instantiates a new workflow file reference.
         */
        public WorkflowFileReference() {
        }

        /**
         * Instantiates a new workflow file reference.
         *
         * @param path
         *            the path
         * @param ref
         *            the ref
         * @param repository_id
         *            the repository id
         * @param sha
         *            the sha
         */
        public WorkflowFileReference(String path, String ref, int repository_id, String sha) {
            this.path = path;
            this.ref = ref;
            this.repository_id = repository_id;
            this.sha = sha;
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
        public int getRepositoryId() {
            return this.repository_id;
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

    /**
     * Code scanning tool parameter.
     */
    public static class CodeScanningTool {
        private AlertsThreshold alerts_threshold;
        private SecurityAlertsThreshold security_alerts_threshold;
        private String tool;

        /**
         * Instantiates a new code scanning tool.
         */
        public CodeScanningTool() {
        }

        /**
         * Instantiates a new code scanning tool.
         *
         * @param alerts_threshold
         *            the alerts threshold
         * @param security_alerts_threshold
         *            the security alerts threshold
         * @param tool
         *            the tool
         */
        public CodeScanningTool(AlertsThreshold alerts_threshold,
                SecurityAlertsThreshold security_alerts_threshold,
                String tool) {
            this.alerts_threshold = alerts_threshold;
            this.security_alerts_threshold = security_alerts_threshold;
            this.tool = tool;
        }

        /**
         * Gets the alerts threshold.
         *
         * @return the alerts threshold
         */
        public AlertsThreshold getAlertsThreshold() {
            return this.alerts_threshold;
        }

        /**
         * Gets the security alerts threshold.
         *
         * @return the security alerts threshold
         */
        public SecurityAlertsThreshold getSecurityAlertsThreshold() {
            return this.security_alerts_threshold;
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
     * Alerts threshold parameter.
     */
    public static enum AlertsThreshold {
        /**
         * none
         */
        none,

        /**
         * errors
         */
        errors,

        /**
         * errors_and_warnings
         */
        errors_and_warnings,

        /**
         * all
         */
        all
    }

    /**
     * Security alerts threshold parameter.
     */
    public static enum SecurityAlertsThreshold {
        /**
         * none
         */
        none,

        /**
         * critical
         */
        critical,

        /**
         * high_or_higher
         */
        high_or_higher,

        /**
         * medium_or_higher
         */
        medium_or_higher,

        /**
         * all
         */
        all
    }
}

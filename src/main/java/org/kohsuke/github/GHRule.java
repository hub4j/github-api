package org.kohsuke.github;

import java.util.Collection;

public class GHRule {
	private RuleType type;
	private Parameters parameters;

	public RuleType getType() {
		return type;
	}

	public void setType(RuleType type) {
		this.type = type;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}


	public GHRule wrap() {
		return this;
	}

	public enum RuleType {
		commit_author_email_pattern,
		creation,
		deletion,
		merge_queue,
		non_fast_forward,
		pull_request,
		require_code_scanning,
		require_deployments,
		require_linear_history,
		required_signatures,
		required_status_checks,
		update,
	}

	public static class Parameters {
		private Collection<RequiredCheck> required_status_checks;
		private MergeMethod merge_method;
		private Integer max_entries_to_build;
		private Integer min_entries_to_merge;
		private Integer max_entries_to_merge;
		private Integer min_entries_to_merge_wait_minutes;
		private Integer check_response_timeout_minutes;
		private GroupingStrategy grouping_strategy;

		public Collection<RequiredCheck> getRequiredStatusChecks() {
			return required_status_checks;
		}

		public void setRequiredStatusChecks(Collection<RequiredCheck> required_status_checks) {
			this.required_status_checks = required_status_checks;
		}

		public MergeMethod getMergeMethod() {
			return merge_method;
		}

		public void setMergeMethod(MergeMethod merge_method) {
			this.merge_method = merge_method;
		}

		public Integer getMaxEntriesToBuild() {
			return max_entries_to_build;
		}

		public void setMaxEntriesToBuild(Integer maxEntriesToBuild) {
			this.max_entries_to_build = maxEntriesToBuild;
		}

		public Integer getMinEntriesToMerge() {
			return min_entries_to_merge;
		}

		public void setMinEntriesToMerge(Integer minEntriesToMerge) {
			this.min_entries_to_merge = minEntriesToMerge;
		}

		public Integer getMaxEntriesToMerge() {
			return max_entries_to_merge;
		}

		public void setMaxEntriesToMerge(Integer maxEntriesToMerge) {
			this.max_entries_to_merge = maxEntriesToMerge;
		}

		public Integer getMinEntriesToMergeWaitMinutes() {
			return min_entries_to_merge_wait_minutes;
		}

		public void setMinEntriesToMergeWaitMinutes(Integer minEntriesToMergeWaitMinutes) {
			this.min_entries_to_merge_wait_minutes = minEntriesToMergeWaitMinutes;
		}

		public Integer getCheckResponseTimeoutMinutes() {
			return check_response_timeout_minutes;
		}

		public void setCheckResponseTimeoutMinutes(Integer checkResponseTimeoutMinutes) {
			this.check_response_timeout_minutes = checkResponseTimeoutMinutes;
		}

		public GroupingStrategy getGroupingStrategy() {
			return grouping_strategy;
		}

		public void setGroupingStrategy(GroupingStrategy groupingStrategy) {
			this.grouping_strategy = groupingStrategy;
		}
	}

	public static class RequiredCheck {
		private String context;

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}
	}

	public enum MergeMethod {
		MERGE,
		SQUASH,
		REBASE
	}

	public enum GroupingStrategy {
		ALLGREEN,
		HEADGREEN
	}
}

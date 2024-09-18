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

		public Collection<RequiredCheck> getRequired_status_checks() {
			return required_status_checks;
		}

		public void setRequired_status_checks(Collection<RequiredCheck> required_status_checks) {
			this.required_status_checks = required_status_checks;
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
}

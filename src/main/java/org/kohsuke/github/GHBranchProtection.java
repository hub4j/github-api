/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collection;

@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
		"URF_UNREAD_FIELD" }, justification = "JSON API")
public class GHBranchProtection {
	@JsonProperty("enforce_admins")
	private EnforceAdmins enforceAdmins;

	@JsonProperty("required_pull_request_reviews")
	private RequiredReviews requiredReviews;

	@JsonProperty("required_status_checks")
	private RequiredStatusChecks requiredStatusChecks;

	@JsonProperty
	private Restrictions restrictions;

	@JsonProperty
	private String url;
	
	public EnforceAdmins getEnforceAdmins() {
        return enforceAdmins;
    }

    public RequiredReviews getRequiredReviews() {
        return requiredReviews;
    }

    public RequiredStatusChecks getRequiredStatusChecks() {
        return requiredStatusChecks;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public String getUrl() {
        return url;
    }

    public static class EnforceAdmins {
		@JsonProperty
		private boolean enabled;

		@JsonProperty
		private String url;

        public String getUrl() {
            return url;
        }

        public boolean isEnabled() {
            return enabled;
        }
	}

	public static class RequiredReviews {
		@JsonProperty("dismissal_restrictions")
		private Restrictions dismissalRestriction; 

		@JsonProperty("dismiss_stale_reviews")
		private boolean dismissStaleReviews;

		@JsonProperty("require_code_owner_reviews")
		private boolean requireCodeOwnerReviews;

		@JsonProperty
		private String url;

        public Restrictions getDismissalRestrictions() {
            return dismissalRestriction;
        }

        public String getUrl() {
            return url;
        }

        public boolean isDismissStaleReviews() {
            return dismissStaleReviews;
        }

        public boolean isRequireCodeOwnerReviews() {
            return requireCodeOwnerReviews;
        }
	}

	public static class RequiredStatusChecks {
		@JsonProperty
		private Collection<String> contexts;

		@JsonProperty
		private boolean strict;

		@JsonProperty
		private String url;

        public Collection<String> getContexts() {
            return contexts;
        }

        public String getUrl() {
            return url;
        }

        public boolean isRequiresBranchUpToDate() {
            return strict;
        }
	}

	public static class Restrictions {
		@JsonProperty
		private Collection<GHTeam> teams;

		@JsonProperty("teams_url")
		private String teamsUrl;

		@JsonProperty
		private String url;

		@JsonProperty
		private Collection<GHUser> users;

		@JsonProperty("users_url")
		private String usersUrl;

        public Collection<GHTeam> getTeams() {
            return teams;
        }

        public String getTeamsUrl() {
            return teamsUrl;
        }

        public String getUrl() {
            return url;
        }

        public Collection<GHUser> getUsers() {
            return users;
        }

        public String getUsersUrl() {
            return usersUrl;
        }
	}
}

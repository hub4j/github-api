package org.kohsuke.github;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Supporting stuff for testing external groups
 */
class ExternalGroupsTestingSupport {

    static GHExternalGroup findExternalGroup(List<GHExternalGroup> groups, Predicate<GHExternalGroup> predicate) {
        return groups.stream().filter(predicate).findFirst().orElseThrow(AssertionError::new);
    }

    static Predicate<GHExternalGroup> hasName(String anObject) {
        return g -> g.getName().equals(anObject);
    }

    static List<String> groupSummary(List<GHExternalGroup> groups) {
        return collect(groups, ExternalGroupsTestingSupport::describeGroup);
    }

    static List<String> teamSummary(GHExternalGroup sut) {
        return collect(sut.getTeams(), ExternalGroupsTestingSupport::describeTeam);
    }

    static List<String> membersSummary(GHExternalGroup sut) {
        return collect(sut.getMembers(), ExternalGroupsTestingSupport::describeMember);
    }

    private static <T> List<String> collect(List<T> collection, Function<T, String> transformation) {
        return collection.stream().map(transformation).collect(Collectors.toList());
    }

    private static String describeGroup(GHExternalGroup g) {
        return String.format("%d:%s", g.getId(), g.getName());
    }

    private static String describeTeam(GHExternalGroup.GHLinkedTeam t) {
        return String.format("%d:%s", t.getId(), t.getName());
    }

    private static String describeMember(GHExternalGroup.GHLinkedExternalMember m) {
        return String.format("%d:%s:%s:%s", m.getId(), m.getLogin(), m.getName(), m.getEmail());
    }

    static class Matchers {

        static Matcher<? super GHExternalGroup> isExternalGroupSummary() {
            return new IsExternalGroupSummary();
        }

    }

    private static class IsExternalGroupSummary extends TypeSafeDiagnosingMatcher<GHExternalGroup> {
        @Override
        protected boolean matchesSafely(GHExternalGroup group, Description mismatchDescription) {
            boolean result = true;
            if (group == null) {
                mismatchDescription.appendText("group is null");
                result = false;
            }
            if (group.getName() == null) {
                mismatchDescription.appendText("name is null");
                result = false;
            }
            if (group.getUpdatedAt() == null) {
                mismatchDescription.appendText("updated at is null");
                result = false;
            }
            if (group.getTeams() == null) {
                mismatchDescription.appendText("teams is null");
                result = false;
            } else if (!group.getTeams().isEmpty()) {
                mismatchDescription.appendText("has teams");
                result = false;
            }
            if (group.getMembers() == null) {
                mismatchDescription.appendText("members is null");
                result = false;
            } else if (!group.getMembers().isEmpty()) {
                mismatchDescription.appendText("has members");
                result = false;
            }
            return result;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is a summary");
        }
    }
}

package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

// TODO: Auto-generated Javadoc
/**
 * The Class GHIssueEventAttributeTest.
 */
public class GHIssueEventAttributeTest extends AbstractGitHubWireMockTest {

    private enum Type implements Predicate<GHIssueEvent>, Consumer<GHIssueEvent> {
        milestone(e -> assertThat(e.getMilestone(), notNullValue()), "milestoned", "demilestoned"),
        label(e -> assertThat(e.getLabel(), notNullValue()), "labeled", "unlabeled"),
        assignment(e -> assertThat(e.getAssignee(), notNullValue()), "assigned", "unassigned");

        private final Consumer<GHIssueEvent> assertion;
        private final Set<String> subtypes;

        Type(final Consumer<GHIssueEvent> assertion, final String... subtypes) {
            this.assertion = assertion;
            this.subtypes = new HashSet<>(asList(subtypes));
        }

        @Override
        public boolean test(final GHIssueEvent event) {
            return this.subtypes.contains(event.getEvent());
        }

        @Override
        public void accept(final GHIssueEvent event) {
            this.assertion.accept(event);
        }
    }

    private List<GHIssueEvent> listEvents(final Type type) throws IOException {
        return StreamSupport
                .stream(gitHub.getRepository("chids/project-milestone-test").getIssue(1).listEvents().spliterator(),
                        false)
                .filter(type)
                .collect(toList());
    }

    /**
     * Test event specific attributes.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testEventSpecificAttributes() throws IOException {
        for (Type type : Type.values()) {
            final List<GHIssueEvent> events = listEvents(type);
            assertThat(events, hasSize(2));
            events.forEach(type);
        }
    }
}

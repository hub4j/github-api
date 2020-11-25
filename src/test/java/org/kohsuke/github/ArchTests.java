package org.kohsuke.github;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import com.tngtech.archunit.core.domain.properties.HasName.AndFullName;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchTests {

    private final JavaClasses classFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages("org.kohsuke.github");

    @Test
    public void testPreviewsAreFlaggedAsDeprecated() {

        String description = "annotate all preview APIs as @Deprecated until they are promoted to stable";

        ArchRule rule = classes().should(new ArchCondition<JavaClass>(description) {

            @Override
            public void check(final JavaClass targetClazz, final ConditionEvents events) {
                checkForPreviewAnnotation(targetClazz, events);
                targetClazz.getAllMethods().forEach(method -> {
                    checkForPreviewAnnotation(method, events);
                });
            }

            <T extends HasAnnotations<T> & AndFullName> void checkForPreviewAnnotation(T codeTarget,
                    ConditionEvents events) {

                if (codeTarget.tryGetAnnotationOfType(Preview.class).isPresent()
                        && !codeTarget.tryGetAnnotationOfType(Deprecated.class).isPresent()) {

                    String message = codeTarget.getFullName()
                            + " uses a preview API and is missing the '@Deprecated' annotation.";

                    events.add(new SimpleConditionEvent(codeTarget, false, message));
                }
            }
        });

        rule.check(classFiles);

    }
}

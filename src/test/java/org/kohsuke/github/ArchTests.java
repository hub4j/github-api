package org.kohsuke.github;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.junit.Assert.assertTrue;

public class ArchTests {

    private static final JavaClasses classFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages("org.kohsuke.github");

    @BeforeClass
    public static void beforeClass() {
        assertTrue(classFiles.size() > 0);
    }

    @Test
    public void testPreviewsAreFlaggedAsDeprecated() {

        String reason = "all preview APIs must be annotated as @Deprecated until they are promoted to stable";

        ArchRule classRule = classes().that()
                .areAnnotatedWith(Preview.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        ArchRule methodRule = methods().that()
                .areAnnotatedWith(Preview.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        ArchRule enumFieldsRule = fields().that()
                .areDeclaredInClassesThat()
                .areEnums()
                .and()
                .areAnnotatedWith(Preview.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        classRule.check(classFiles);
        enumFieldsRule.check(classFiles);
        methodRule.check(classFiles);

    }
}

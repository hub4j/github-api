package org.kohsuke.github;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.junit.Assert.assertTrue;

public class ArchTests {

    private static final JavaClasses classFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages("org.kohsuke.github");

    private static final DescribedPredicate<JavaAnnotation<?>> previewAnnotationWithNoMediaType = new DescribedPredicate<JavaAnnotation<?>>(
            "preview has no required media types defined") {

        @Override
        public boolean apply(JavaAnnotation<?> javaAnnotation) {
            boolean isPreview = javaAnnotation.getRawType().isEquivalentTo(Preview.class);
            Object[] values = (Object[]) javaAnnotation.getProperties().get("value");
            return isPreview && values != null && values.length < 1;
        }
    };

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
                .andShould(not(beAnnotatedWith(previewAnnotationWithNoMediaType)))
                .because(reason);

        ArchRule methodRule = methods().that()
                .areAnnotatedWith(Preview.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .andShould(not(beAnnotatedWith(previewAnnotationWithNoMediaType)))
                .because(reason);

        ArchRule enumFieldsRule = fields().that()
                .areDeclaredInClassesThat()
                .areEnums()
                .and()
                .areAnnotatedWith(Preview.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .andShould(not(beAnnotatedWith(previewAnnotationWithNoMediaType)))
                .because(reason);

        classRule.check(classFiles);
        enumFieldsRule.check(classFiles);
        methodRule.check(classFiles);

    }

    @Test
    public void testBetaApisAreFlaggedAsDeprecated() {

        String reason = "all beta APIs must be annotated as @Deprecated until they are promoted to stable";

        ArchRule classRule = classes().that()
                .areAnnotatedWith(BetaApi.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        ArchRule methodRule = methods().that()
                .areAnnotatedWith(BetaApi.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        ArchRule enumFieldsRule = fields().that()
                .areDeclaredInClassesThat()
                .areEnums()
                .and()
                .areAnnotatedWith(BetaApi.class)
                .should()
                .beAnnotatedWith(Deprecated.class)
                .because(reason);

        classRule.check(classFiles);
        enumFieldsRule.check(classFiles);
        methodRule.check(classFiles);

    }
}

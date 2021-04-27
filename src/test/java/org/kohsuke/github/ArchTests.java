package org.kohsuke.github;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.HasOwner;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameContaining;
import static com.tngtech.archunit.lang.conditions.ArchConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class ArchTests {

    private static final JavaClasses classFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages("org.kohsuke.github");

    private static final JavaClasses apacheCommons = new ClassFileImporter().importPackages("org.apache.commons.lang3");

    private static final JavaClasses testClassFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
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
        assertThat(classFiles.size(), greaterThan(0));
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

    @Test
    public void testRequireUseOfAssertThat() {

        final String reason = "This project uses `assertThat(...)` instead of other `assert*()` methods.";

        final DescribedPredicate<HasName> assertMethodOtherThanAssertThat = nameContaining("assert")
                .and(DescribedPredicate.not(name("assertThat")));

        final ArchRule onlyAssertThatRule = classes()
                .should(not(callMethodWhere(target(assertMethodOtherThanAssertThat))))
                .because(reason);

        onlyAssertThatRule.check(testClassFiles);
    }

    @Test
    public void testRequireUseOfOnlySpecificApacheCommons() {

        final DescribedPredicate<JavaCall<?>> approvedStringUtilsMethods = target(
                HasOwner.Predicates.With.<JavaClass>owner(name("org.apache.commons.lang3.StringUtils")))
                        .and(target(name("prependIfMissing").or(name("isBlank"))
                                .or(name("isEmpty"))
                                .or(name("equals"))
                                .or(name("capitalize"))
                                .or(name("join"))
                                .or(name("defaultString"))));
        final DescribedPredicate<JavaCall<?>> approvedToStringBuilderMethods = target(
                HasOwner.Predicates.With.<JavaClass>owner(name("org.apache.commons.lang3.builder.ToStringBuilder")))
                        .and(target(name("toString").or(name("append"))
                                .or(name("isEmpty"))
                                .or(name("equals"))
                                .or(name("capitalize"))));
        final DescribedPredicate<JavaCall<?>> approvedToStringStyleMethods = target(
                HasOwner.Predicates.With.<JavaClass>owner(name("org.apache.commons.lang3.builder.ToStringStyle")))
                        .and(target(name("append")));
        final DescribedPredicate<JavaCall<?>> approvedReflectionStringBuilderMethods = target(HasOwner.Predicates.With
                .<JavaClass>owner(name("org.apache.commons.lang3.builder.ReflectionToStringBuilder")))
                        .and(target(name("accept")));

        final DescribedPredicate<JavaCall<?>> approvedIOUtilsMethods = target(
                HasOwner.Predicates.With.<JavaClass>owner(name("org.apache.commons.io.IOUtils")))
                        .and(target(name("closeQuietly").or(name("toString")).or(name("toByteArray"))));

        final DescribedPredicate<JavaCall<?>> approvedApacheCommonsMethods = approvedStringUtilsMethods
                .or(approvedToStringBuilderMethods)
                .or(approvedToStringStyleMethods)
                .or(approvedReflectionStringBuilderMethods)
                .or(approvedIOUtilsMethods);

        final ArchRule onlyApprovedApacheCommonsLang3Methods = classes()
                .should(not(callMethodWhere(
                        target(HasOwner.Predicates.With.<JavaClass>owner(resideInAPackage("org.apache.commons..")))
                                .and(DescribedPredicate.not(approvedApacheCommonsMethods)))))
                .because(
                        "Only commons methods that have been manually verified to be compatible with v2.4 should be used.");

        onlyApprovedApacheCommonsLang3Methods.check(classFiles);
    }

}

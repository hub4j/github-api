package org.kohsuke.github;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.HasOwner;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.core.domain.JavaClass.namesOf;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameContaining;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.core.domain.properties.HasParameterTypes.Predicates.rawParameterTypes;
import static com.tngtech.archunit.lang.conditions.ArchConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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
    public void testRequireUseOfAssertThat() {

        final String reason = "This project uses `assertThat(...)` or `assertThrows(...)` instead of other `assert*()` methods.";

        final DescribedPredicate<HasName> assertMethodOtherThanAssertThat = nameContaining("assert")
                .and(DescribedPredicate.not(name("assertThat")).and(DescribedPredicate.not(name("assertThrows"))));

        final ArchRule onlyAssertThatRule = classes()
                .should(not(callMethodWhere(target(assertMethodOtherThanAssertThat))))
                .because(reason);

        onlyAssertThatRule.check(testClassFiles);
    }

    @Test
    public void testApiStability() {
        assertThat("OkHttpConnector must implement HttpConnector",
                Arrays.asList(OkHttpConnector.class.getInterfaces()),
                Matchers.containsInAnyOrder(HttpConnector.class));
    }

    @Test
    public void testRequireUseOfOnlySpecificApacheCommons() {

        final ArchRule onlyApprovedApacheCommonsMethods = classes()
                .should(notCallMethodsInPackageUnless("org.apache.commons..",
                        // unless it is one of these methods
                        targetMethodIs(StringUtils.class, "capitalize", String.class),
                        targetMethodIs(StringUtils.class, "defaultString", String.class, String.class),
                        targetMethodIs(StringUtils.class, "equals", CharSequence.class, CharSequence.class),
                        targetMethodIs(StringUtils.class, "isBlank", CharSequence.class),
                        targetMethodIs(StringUtils.class, "isEmpty", CharSequence.class),
                        targetMethodIs(StringUtils.class, "join", Iterable.class, String.class),
                        targetMethodIs(StringUtils.class,
                                "prependIfMissing",
                                String.class,
                                CharSequence.class,
                                CharSequence[].class),
                        targetMethodIs(ToStringBuilder.class, "toString"),
                        targetMethodIs(ToStringBuilder.class, "append", String.class, Object.class),
                        targetMethodIs(ToStringBuilder.class, "append", String.class, long.class),
                        targetMethodIs(ToStringBuilder.class, "append", String.class, int.class),
                        targetMethodIs(ToStringBuilder.class, "isEmpty"),
                        targetMethodIs(ToStringBuilder.class, "equals"),
                        targetMethodIs(ToStringBuilder.class, "capitalize"),
                        targetMethodIs(ToStringStyle.class,
                                "append",
                                StringBuffer.class,
                                String.class,
                                Object.class,
                                Boolean.class),
                        targetMethodIs(ReflectionToStringBuilder.class, "accept", Field.class),
                        targetMethodIs(IOUtils.class, "closeQuietly", InputStream.class),
                        targetMethodIs(IOUtils.class, "closeQuietly", Closeable.class),
                        targetMethodIs(IOUtils.class, "copyLarge", InputStream.class, OutputStream.class),
                        targetMethodIs(IOUtils.class, "toString", InputStream.class, Charset.class),
                        targetMethodIs(IOUtils.class, "toString", Reader.class),
                        targetMethodIs(IOUtils.class, "toByteArray", InputStream.class),
                        targetMethodIs(IOUtils.class, "write", byte[].class, OutputStream.class)))
                .because(
                        "Commons methods must be manually verified to be compatible with commons-io:2.4 or earlier and commons-lang3:3.9 or earlier.");

        onlyApprovedApacheCommonsMethods.check(classFiles);
    }

    public static ArchCondition<JavaClass> notCallMethodsInPackageUnless(final String packageIdentifier,
            final DescribedPredicate<JavaCall<?>>... unlessPredicates) {
        DescribedPredicate<JavaCall<?>> restrictedPackageCalls = target(
                HasOwner.Predicates.With.<JavaClass>owner(resideInAPackage(packageIdentifier)));

        if (unlessPredicates.length > 0) {
            DescribedPredicate<JavaCall<?>> allowed = unlessPredicates[0];
            for (int x = 1; x < unlessPredicates.length; x++) {
                allowed = allowed.or(unlessPredicates[x]);
            }
            restrictedPackageCalls = unless(restrictedPackageCalls, allowed);
        }
        return not(callMethodWhere(restrictedPackageCalls));
    }

    public static DescribedPredicate<JavaCall<?>> targetMethodIs(Class<?> owner,
            String methodName,
            Class<?>... parameterTypes) {
        return JavaCall.Predicates.target(owner(type(owner)))
                .and(JavaCall.Predicates.target(name(methodName)))
                .and(JavaCall.Predicates.target(rawParameterTypes(parameterTypes)))
                .as("method is %s",
                        Formatters.formatMethodSimple(owner.getSimpleName(), methodName, namesOf(parameterTypes)));
    }

    public static <T> DescribedPredicate<T> unless(DescribedPredicate<? super T> first,
            DescribedPredicate<? super T> second) {
        return new UnlessPredicate(first, second);
    }

    private static class UnlessPredicate<T> extends DescribedPredicate<T> {
        private final DescribedPredicate<T> current;
        private final DescribedPredicate<? super T> other;

        UnlessPredicate(DescribedPredicate<T> current, DescribedPredicate<? super T> other) {
            super(current.getDescription() + " unless " + other.getDescription());
            this.current = checkNotNull(current);
            this.other = checkNotNull(other);
        }

        @Override
        public boolean apply(T input) {
            return current.apply(input) && !other.apply(input);
        }
    }
}

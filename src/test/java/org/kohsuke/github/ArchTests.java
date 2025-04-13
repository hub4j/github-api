package org.kohsuke.github;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.HasOwner;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.conditions.ArchConditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kohsuke.github.GHDiscussion.Creator;
import org.kohsuke.github.GHPullRequestCommitDetail.Commit;
import org.kohsuke.github.GHPullRequestCommitDetail.CommitPointer;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameContaining;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.core.domain.properties.HasParameterTypes.Predicates.rawParameterTypes;
import static com.tngtech.archunit.lang.conditions.ArchConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

// TODO: Auto-generated Javadoc
/**
 * The Class ArchTests.
 */
@SuppressWarnings({ "LocalVariableNamingConvention", "TestMethodWithoutAssertion", "UnqualifiedStaticUsage",
        "unchecked", "MethodMayBeStatic", "FieldNamingConvention", "StaticCollection" })
public class ArchTests {

    private static final class EnumConstantFieldPredicate extends DescribedPredicate<JavaField> {
        private EnumConstantFieldPredicate() {
            super("are not enum constants");
        }

        @Override
        public boolean test(JavaField javaField) {
            JavaClass owner = javaField.getOwner();
            return owner.isEnum() && javaField.getRawType().isAssignableTo(owner.reflect());
        }
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
        public boolean test(T input) {
            return current.test(input) && !other.test(input);
        }
    }

    private static final JavaClasses apacheCommons = new ClassFileImporter().importPackages("org.apache.commons.lang3");

    private static final JavaClasses classFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("org.kohsuke.github");

    private static final JavaClasses testClassFiles = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages("org.kohsuke.github");

    /**
     * Before class.
     */
    @BeforeClass
    public static void beforeClass() {
        assertThat(classFiles.size(), greaterThan(0));
    }

    /**
     * Have names containing unless.
     *
     * @param <T>
     *            the generic type
     * @param infix
     *            the infix
     * @param unlessPredicates
     *            the unless predicates
     * @return the arch condition
     */
    public static <T extends HasDescription & HasSourceCodeLocation & HasName> ArchCondition<T> haveNamesContainingUnless(
            final String infix,
            final DescribedPredicate<? super T>... unlessPredicates) {
        DescribedPredicate<? super T> restrictedNameContaining = nameContaining(infix);

        if (unlessPredicates.length > 0) {
            final DescribedPredicate<T> allowed = or(unlessPredicates);
            restrictedNameContaining = unless(nameContaining(infix), allowed);
        }
        return have(restrictedNameContaining);
    }

    /**
     * Not call methods in package unless.
     *
     * @param packageIdentifier
     *            the package identifier
     * @param unlessPredicates
     *            the unless predicates
     * @return the arch condition
     */
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
        return ArchConditions.not(callMethodWhere(restrictedPackageCalls));
    }

    /**
     * Target method is.
     *
     * @param owner
     *            the owner
     * @param methodName
     *            the method name
     * @param parameterTypes
     *            the parameter types
     * @return the described predicate
     */
    public static DescribedPredicate<JavaCall<?>> targetMethodIs(Class<?> owner,
            String methodName,
            Class<?>... parameterTypes) {
        return JavaCall.Predicates.target(owner(type(owner)))
                .and(JavaCall.Predicates.target(name(methodName)))
                .and(JavaCall.Predicates.target(rawParameterTypes(parameterTypes)))
                .as("method is %s",
                        Formatters.formatMethodSimple(owner.getSimpleName(),
                                methodName,
                                Arrays.stream(parameterTypes)
                                        .map(item -> item.getName())
                                        .collect(Collectors.toList())));
    }

    /**
     * Unless.
     *
     * @param <T>
     *            the generic type
     * @param first
     *            the first
     * @param second
     *            the second
     * @return the described predicate
     */
    public static <T> DescribedPredicate<T> unless(DescribedPredicate<? super T> first,
            DescribedPredicate<? super T> second) {
        return new UnlessPredicate(first, second);
    }

    private DescribedPredicate<JavaField> and;

    /**
     * Default constructor.
     */
    public ArchTests() {
    }

    /**
     * Test naming conventions
     */
    @Test
    public void testRequireFollowingNamingConvention() {
        final String reason = "This project follows standard java naming conventions and does not allow the use of underscores in names.";

        final ArchRule constantFieldsShouldFollowConvention = fields().that()
                .areStatic()
                .and()
                .areFinal()
                .should(haveNameMatching("[a-zA-Z$][a-zA-Z0-9$_]*"))
                .because(reason);

        final ArchRule enumsShouldFollowConvention = fields().that(enumConstants())
                .and(not(declaredIn(GHCompare.Status.class)))
                .should(haveNameMatching("[A-Z][A-Z0-9_]*"))
                .because("This project follows standard java naming conventions for enums.");

        var notStaticFinalFields = DescribedPredicate.<JavaField>not(modifier(STATIC).and(modifier(STATIC)));
        var notEnumOrStaticFinalFields = DescribedPredicate.<JavaField>and(not(enumConstants()), notStaticFinalFields);

        final ArchRule instanceFieldsShouldNotBePublic = fields().that(notEnumOrStaticFinalFields)
                .should(notHaveModifier(JavaModifier.PUBLIC))
                .because("This project does not allow public instance fields.");

        final ArchRule instanceFieldsShouldFollowConvention = noFields().that(notEnumOrStaticFinalFields)
                .should(haveNamesContainingUnless("_"))
                .because("This project follows standard java naming conventions for fields.");

        @SuppressWarnings("AccessStaticViaInstance")
        final ArchRule methodsNotFollowingConvention = noMethods().that()
                .arePublic()
                .should(haveNamesContainingUnless("_",
                        // currently failing method names
                        // TODO: 2025-03-28 Fix & remove these
                        declaredIn(assignableTo(PagedIterable.class)).and(name("_iterator")),
                        declaredIn(GHCompare.class).and(name("getAdded_by")),
                        declaredIn(GHDeployKey.class).and(name("getAdded_by")),
                        declaredIn(GHDeployKey.class).and(name("isRead_only")),
                        declaredIn(assignableTo(GHRepositoryBuilder.class)).and(name("private_")),
                        declaredIn(Creator.class).and(name("private_")),
                        declaredIn(GHGistBuilder.class).and(name("public_")),
                        declaredIn(Commit.class).and(name("getComment_count")),
                        declaredIn(CommitPointer.class).and(name("getHtml_url")),
                        declaredIn(GHRelease.class).and(name("getPublished_at"))))
                .because(reason);

        final ArchRule classesNotFollowingConvention = noClasses().should(haveNamesContainingUnless("_"))
                .because(reason);

        enumsShouldFollowConvention.check(classFiles);
        constantFieldsShouldFollowConvention.check(classFiles);
        instanceFieldsShouldNotBePublic.check(classFiles);
        instanceFieldsShouldFollowConvention.check(classFiles);
        methodsNotFollowingConvention.check(classFiles);
        classesNotFollowingConvention.check(classFiles);
    }

    /**
     * Test require use of assert that.
     */
    @Test
    public void testRequireUseOfAssertThat() {

        final String reason = "This project uses `assertThat(...)` or `assertThrows(...)` instead of other `assert*()` methods.";

        final DescribedPredicate<HasName> assertMethodOtherThanAssertThat = nameContaining("assert")
                .and(not(name("assertThat")).and(not(name("assertThrows"))));

        final ArchRule onlyAssertThatRule = classes()
                .should(ArchConditions.not(callMethodWhere(target(assertMethodOtherThanAssertThat))))
                .because(reason);

        onlyAssertThatRule.check(testClassFiles);
    }

    /**
     * Test require use of only specific apache commons.
     */
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
                        targetMethodIs(ToStringBuilder.class, "append", String.class, boolean.class),
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

    /**
     * Enum constants.
     *
     * @return the described predicate
     */
    private DescribedPredicate<? super JavaField> enumConstants() {
        return new EnumConstantFieldPredicate();
    }
}

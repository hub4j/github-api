package org.kohsuke.github.internal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class EnumUtilsTest.
 */
public class EnumUtilsTest {

    /**
     * Test get enum.
     */
    @Test
    public void testGetEnum() {
        assertThat(EnumUtils.getEnumOrDefault(TestEnum.class, null, TestEnum.UNKNOWN), equalTo(TestEnum.UNKNOWN));

        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, null, TestEnum.UNKNOWN), nullValue());
        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, "foobar", TestEnum.UNKNOWN),
                equalTo(TestEnum.UNKNOWN));
        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, "VALUE_1", TestEnum.UNKNOWN),
                equalTo(TestEnum.VALUE_1));
        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, "value_1", TestEnum.UNKNOWN),
                equalTo(TestEnum.VALUE_1));
        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, "VALUE_2", TestEnum.UNKNOWN),
                equalTo(TestEnum.VALUE_2));
        assertThat(EnumUtils.getNullableEnumOrDefault(TestEnum.class, "vAlUe_2", TestEnum.UNKNOWN),
                equalTo(TestEnum.VALUE_2));
    }

    private enum TestEnum {
        VALUE_1, VALUE_2, UNKNOWN;
    }
}

package org.kohsuke.github.internal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnumUtilsTest {

    @Test
    public void testGetEnum() {
        assertNull(EnumUtils.getNullableEnumOrDefault(TestEnum.class, null, TestEnum.UNKNOWN));
        assertEquals(TestEnum.UNKNOWN, EnumUtils.getNullableEnumOrDefault(TestEnum.class, "foobar", TestEnum.UNKNOWN));
        assertEquals(TestEnum.VALUE_1, EnumUtils.getNullableEnumOrDefault(TestEnum.class, "VALUE_1", TestEnum.UNKNOWN));
        assertEquals(TestEnum.VALUE_1, EnumUtils.getNullableEnumOrDefault(TestEnum.class, "value_1", TestEnum.UNKNOWN));
        assertEquals(TestEnum.VALUE_2, EnumUtils.getNullableEnumOrDefault(TestEnum.class, "VALUE_2", TestEnum.UNKNOWN));
        assertEquals(TestEnum.VALUE_2, EnumUtils.getNullableEnumOrDefault(TestEnum.class, "value_2", TestEnum.UNKNOWN));
    }

    private enum TestEnum {
        VALUE_1, VALUE_2, UNKNOWN;
    }
}

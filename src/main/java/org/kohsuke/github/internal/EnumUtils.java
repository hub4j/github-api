package org.kohsuke.github.internal;

import java.util.Locale;

/**
 * Utils for Enums.
 */
public final class EnumUtils {

    /**
     * Returns an enum value matching the value if found, null if the value is null and {@code defaultEnum} if the value
     * cannot be matched to a value of the enum.
     * <p>
     * The value is converted to uppercase before being matched to the enum values.
     *
     * @param <E>
     *            the type of the enum
     * @param enumClass
     *            the type of the enum
     * @param value
     *            the value to interpret
     * @param defaultEnum
     *            the default enum value if the value doesn't match one of the enum value
     * @return an enum value or null
     */
    public static <E extends Enum<E>> E getNullableEnumOrDefault(Class<E> enumClass, String value, E defaultEnum) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return defaultEnum;
        }
    }

    private EnumUtils() {
    }
}

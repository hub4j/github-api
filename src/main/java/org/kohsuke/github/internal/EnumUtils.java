package org.kohsuke.github.internal;

import java.util.Locale;

public final class EnumUtils {

    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String value, E defaultEnum) {
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

package me.dustin.jex.helper.world;

import org.apache.commons.lang3.StringUtils;

import java.util.OptionalLong;

public enum SeedHelper {
    INSTANCE;
    public OptionalLong getSeed(String string) {
        OptionalLong optionalLong4;
        if (StringUtils.isEmpty(string)) {
            optionalLong4 = OptionalLong.empty();
        } else {
            OptionalLong optionalLong2 = tryParseLong(string);
            if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
                optionalLong4 = optionalLong2;
            } else {
                optionalLong4 = OptionalLong.of(string.hashCode());
            }
        }

        return optionalLong4;
    }

    public static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException var2) {
            return OptionalLong.empty();
        }
    }
}

package com.daxia.betterinput;

import net.minecraft.util.Formatting;

public final class BetterInputFormatting {
    public static final String PREFIX = "\u00A7";
    public static final String RESET = PREFIX + "r";

    private BetterInputFormatting() {
    }

    public static String code(Formatting formatting) {
        return PREFIX + formatting.getCode();
    }

    public static String code(char formattingCode) {
        return PREFIX + formattingCode;
    }
}

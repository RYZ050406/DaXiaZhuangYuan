package com.daxia.betterinput;

import java.util.List;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LegacyBookTextParser {
    private LegacyBookTextParser() {
    }

    public static Text parse(String raw, List<BetterInputPayloads.BookLink> links) {
        MutableText result = Text.empty();
        Style style = Style.EMPTY;

        for (int index = 0; index < raw.length(); index++) {
            char current = raw.charAt(index);
            if (current == Formatting.FORMATTING_CODE_PREFIX && index + 1 < raw.length()) {
                Formatting formatting = Formatting.byCode(raw.charAt(index + 1));
                if (formatting != null) {
                    style = applyFormatting(style, formatting);
                    index++;
                    continue;
                }
            }

            Style effectiveStyle = withLink(style, links, index);
            result.append(Text.literal(String.valueOf(current)).setStyle(effectiveStyle));
        }

        return result;
    }

    private static Style applyFormatting(Style style, Formatting formatting) {
        if (formatting == Formatting.RESET) {
            return Style.EMPTY;
        }

        if (formatting.isColor()) {
            return Style.EMPTY.withColor(formatting);
        }

        return style.withFormatting(formatting);
    }

    private static Style withLink(Style style, List<BetterInputPayloads.BookLink> links, int rawIndex) {
        for (BetterInputPayloads.BookLink link : links) {
            if (rawIndex >= link.start() && rawIndex < link.end()) {
                return style.withClickEvent(new ClickEvent.RunCommand(normalizeCommand(link.command())));
            }
        }

        return style;
    }

    private static String normalizeCommand(String command) {
        String trimmed = command.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("/")) {
            return trimmed;
        }

        return "/" + trimmed;
    }
}

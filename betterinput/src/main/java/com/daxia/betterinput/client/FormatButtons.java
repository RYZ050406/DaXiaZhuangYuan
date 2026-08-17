package com.daxia.betterinput.client;

import com.daxia.betterinput.BetterInputFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class FormatButtons {
    private static final Formatting[] COLORS = {
            Formatting.BLACK,
            Formatting.DARK_BLUE,
            Formatting.DARK_GREEN,
            Formatting.DARK_AQUA,
            Formatting.DARK_RED,
            Formatting.DARK_PURPLE,
            Formatting.GOLD,
            Formatting.GRAY,
            Formatting.DARK_GRAY,
            Formatting.BLUE,
            Formatting.GREEN,
            Formatting.AQUA,
            Formatting.RED,
            Formatting.LIGHT_PURPLE,
            Formatting.YELLOW,
            Formatting.WHITE
    };

    private FormatButtons() {
    }

    public static List<ButtonWidget> create(int x, int y, boolean includeLink, Consumer<String> formatter, Runnable linkAction) {
        List<ButtonWidget> buttons = new ArrayList<>();
        for (int index = 0; index < COLORS.length; index++) {
            Formatting color = COLORS[index];
            int buttonX = x + (index % 8) * 18;
            int buttonY = y + (index / 8) * 18;
            buttons.add(ButtonWidget.builder(
                    Text.literal("\u25A0").formatted(color),
                    button -> formatter.accept(BetterInputFormatting.code(color))
            ).dimensions(buttonX, buttonY, 18, 18).build());
        }

        int styleY = y + 42;
        addStyleButton(buttons, x, styleY, "B", BetterInputFormatting.code('l'), Formatting.BOLD, formatter);
        addStyleButton(buttons, x + 24, styleY, "I", BetterInputFormatting.code('o'), Formatting.ITALIC, formatter);
        addStyleButton(buttons, x + 48, styleY, "U", BetterInputFormatting.code('n'), Formatting.UNDERLINE, formatter);
        addStyleButton(buttons, x + 72, styleY, "S", BetterInputFormatting.code('m'), Formatting.STRIKETHROUGH, formatter);
        addStyleButton(buttons, x + 96, styleY, "K", BetterInputFormatting.code('k'), Formatting.OBFUSCATED, formatter);
        buttons.add(ButtonWidget.builder(
                Text.literal("R").formatted(Formatting.WHITE),
                button -> formatter.accept(BetterInputFormatting.RESET)
        ).dimensions(x + 120, styleY, 22, 18).build());

        if (includeLink) {
            buttons.add(ButtonWidget.builder(
                    Text.translatable("button.betterinput.book_command").formatted(Formatting.AQUA, Formatting.UNDERLINE),
                    button -> linkAction.run()
            ).dimensions(x, styleY + 24, 70, 18).build());
        }

        return buttons;
    }

    private static void addStyleButton(List<ButtonWidget> buttons, int x, int y, String label, String code, Formatting formatting, Consumer<String> formatter) {
        buttons.add(ButtonWidget.builder(
                Text.literal(label).formatted(formatting),
                button -> formatter.accept(code)
        ).dimensions(x, y, 22, 18).build());
    }
}

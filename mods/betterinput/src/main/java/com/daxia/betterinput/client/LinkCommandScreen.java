package com.daxia.betterinput.client;

import java.util.function.Consumer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class LinkCommandScreen extends Screen {
    private final Screen parent;
    private final String initialCommand;
    private final boolean allowEmptyCommand;
    private final Consumer<String> callback;
    private TextFieldWidget commandField;
    private ChatInputSuggestor commandSuggestor;
    private String currentCommand;

    public LinkCommandScreen(Screen parent, Consumer<String> callback) {
        this(parent, "screen.betterinput.book_command.title", "", false, callback);
    }

    public LinkCommandScreen(Screen parent, String titleKey, String initialCommand, boolean allowEmptyCommand, Consumer<String> callback) {
        super(Text.translatable(titleKey));
        this.parent = parent;
        this.initialCommand = initialCommand;
        this.allowEmptyCommand = allowEmptyCommand;
        this.callback = callback;
        this.currentCommand = initialCommand;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldY = 50;

        this.commandField = new TextFieldWidget(
                this.textRenderer,
                centerX - 150,
                fieldY,
                300,
                20,
                Text.translatable("screen.betterinput.command")
        );
        this.commandField.setMaxLength(256);
        this.commandField.setPlaceholder(Text.literal("/say hello"));
        this.commandField.setText(this.currentCommand == null ? this.initialCommand : this.currentCommand);
        this.addDrawableChild(this.commandField);
        this.setInitialFocus(this.commandField);

        this.commandSuggestor = new ChatInputSuggestor(
                this.client,
                this,
                this.commandField,
                this.textRenderer,
                true,
                true,
                0,
                7,
                false,
                0x80000000
        );
        this.commandSuggestor.setWindowActive(true);
        this.commandField.setChangedListener(value -> {
            this.currentCommand = value;
            this.commandSuggestor.refresh();
        });
        this.commandSuggestor.refresh();

        int buttonY = Math.min(this.height - 32, 168);
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.betterinput.link.done"),
                button -> apply()
        ).dimensions(centerX - 105, buttonY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.betterinput.link.cancel"),
                button -> this.client.setScreen(this.parent)
        ).dimensions(centerX + 5, buttonY, 100, 20).build());
    }

    private void apply() {
        String command = this.commandField.getText().trim();
        if (this.allowEmptyCommand || !command.isEmpty()) {
            this.callback.accept(command);
        }
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (this.commandSuggestor != null && this.commandSuggestor.keyPressed(input)) {
            return true;
        }

        if (super.keyPressed(input)) {
            return true;
        }

        if (input.isEnter()) {
            apply();
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        if (super.charTyped(input)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.commandSuggestor != null && this.commandSuggestor.mouseScrolled(verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.commandSuggestor != null && this.commandSuggestor.mouseClicked(click)) {
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.betterinput.command"), this.width / 2 - 149, 40, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);
        if (this.commandSuggestor != null) {
            this.commandSuggestor.render(context, mouseX, mouseY);
        }
    }
}

package com.daxia.betterinput.client;

import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class LinkCommandScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> callback;
    private TextFieldWidget commandField;

    public LinkCommandScreen(Screen parent, Consumer<String> callback) {
        super(Text.translatable("screen.betterinput.link.title"));
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.commandField = new TextFieldWidget(
                this.textRenderer,
                centerX - 140,
                centerY - 10,
                280,
                20,
                Text.translatable("screen.betterinput.link.command")
        );
        this.commandField.setMaxLength(256);
        this.commandField.setPlaceholder(Text.literal("/say hello"));
        this.addDrawableChild(this.commandField);
        this.setInitialFocus(this.commandField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.betterinput.link.done"),
                button -> apply()
        ).dimensions(centerX - 105, centerY + 22, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.betterinput.link.cancel"),
                button -> this.client.setScreen(this.parent)
        ).dimensions(centerX + 5, centerY + 22, 100, 20).build());
    }

    private void apply() {
        String command = this.commandField.getText().trim();
        if (!command.isEmpty()) {
            this.callback.accept(command);
        }
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (this.commandField != null && this.commandField.keyPressed(input)) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        if (this.commandField != null && this.commandField.charTyped(input)) {
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 38, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}

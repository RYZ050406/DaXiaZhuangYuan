package com.daxia.action.client;

import com.daxia.action.ActionType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ActionPanelScreen extends Screen {
    public ActionPanelScreen() {
        super(Text.translatable("screen.action.title"));
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        int buttonHeight = 20;
        int gap = 8;
        int centerX = this.width / 2;
        int startY = Math.max(38, this.height / 2 - 74);

        ActionType[] actions = ActionType.menuActions();
        for (int index = 0; index < actions.length; index++) {
            ActionType action = actions[index];
            int column = index % 2;
            int row = index / 2;
            int x = centerX - buttonWidth - gap / 2 + column * (buttonWidth + gap);
            int y = startY + row * (buttonHeight + gap);
            addActionButton(action, x, y, buttonWidth, buttonHeight);
        }

        int bottomY = startY + 4 * (buttonHeight + gap) + 8;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(ActionType.NONE.translationKey()).formatted(Formatting.RED, Formatting.BOLD),
                button -> select(ActionType.NONE)
        ).dimensions(centerX - buttonWidth / 2, bottomY, buttonWidth, buttonHeight).build());
    }

    private void addActionButton(ActionType action, int x, int y, int width, int height) {
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(action.translationKey()).formatted(buttonColor(action)),
                button -> select(action)
        ).dimensions(x, y, width, height).build());
    }

    private Formatting buttonColor(ActionType action) {
        return switch (action) {
            case RAISE_RIGHT, RAISE_LEFT -> Formatting.AQUA;
            case WAVE_RIGHT, WAVE_LEFT -> Formatting.LIGHT_PURPLE;
            case SIT -> Formatting.YELLOW;
            case LIE -> Formatting.BLUE;
            case PRONE -> Formatting.GREEN;
            case NONE -> Formatting.WHITE;
        };
    }

    private void select(ActionType action) {
        ActionClient.requestAction(action);
        this.client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xAA101018);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFF55);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

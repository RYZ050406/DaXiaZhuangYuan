package com.daxia.betterinput.client;

import com.daxia.betterinput.BetterInputPayloads;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BookCommandListScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 54;
    private final Screen parent;
    private final List<BetterInputPayloads.BookLink> links;
    private int page;

    public BookCommandListScreen(Screen parent, List<BetterInputPayloads.BookLink> links) {
        super(Text.translatable("screen.betterinput.command_list.title"));
        this.parent = parent;
        this.links = links;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int pageSize = getPageSize();
        int maxPage = getMaxPage(pageSize);
        this.page = Math.min(this.page, maxPage);

        int start = this.page * pageSize;
        int end = Math.min(this.links.size(), start + pageSize);
        for (int index = start; index < end; index++) {
            int localIndex = index - start;
            int rowY = LIST_TOP + localIndex * ROW_HEIGHT;
            int linkIndex = index;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("screen.betterinput.command_list.edit"),
                    button -> editCommand(linkIndex)
            ).dimensions(centerX + 64, rowY - 4, 44, 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("screen.betterinput.command_list.remove"),
                    button -> removeCommand(linkIndex)
            ).dimensions(centerX + 112, rowY - 4, 44, 20).build());
        }

        ButtonWidget previous = ButtonWidget.builder(
                Text.translatable("screen.betterinput.command_list.previous"),
                button -> {
                    this.page--;
                    this.clearAndInit();
                }
        ).dimensions(centerX - 154, this.height - 28, 70, 20).build();
        previous.active = this.page > 0;
        this.addDrawableChild(previous);

        ButtonWidget next = ButtonWidget.builder(
                Text.translatable("screen.betterinput.command_list.next"),
                button -> {
                    this.page++;
                    this.clearAndInit();
                }
        ).dimensions(centerX - 78, this.height - 28, 70, 20).build();
        next.active = this.page < maxPage;
        this.addDrawableChild(next);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.betterinput.command_list.back"),
                button -> this.client.setScreen(this.parent)
        ).dimensions(centerX + 84, this.height - 28, 70, 20).build());
    }

    private void editCommand(int index) {
        if (index < 0 || index >= this.links.size()) {
            return;
        }

        BetterInputPayloads.BookLink link = this.links.get(index);
        this.client.setScreen(new LinkCommandScreen(
                this,
                "screen.betterinput.command_list.edit_title",
                link.command(),
                false,
                command -> {
                    if (index >= 0 && index < this.links.size()) {
                        this.links.set(index, new BetterInputPayloads.BookLink(
                                link.page(),
                                link.start(),
                                link.end(),
                                command
                        ));
                    }
                }
        ));
    }

    private void removeCommand(int index) {
        if (index < 0 || index >= this.links.size()) {
            return;
        }

        this.links.remove(index);
        this.page = Math.min(this.page, getMaxPage(getPageSize()));
        this.clearAndInit();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxPage = getMaxPage(getPageSize());
        if (verticalAmount < 0.0D && this.page < maxPage) {
            this.page++;
            this.clearAndInit();
            return true;
        }

        if (verticalAmount > 0.0D && this.page > 0) {
            this.page--;
            this.clearAndInit();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        if (this.links.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable("screen.betterinput.command_list.empty"),
                    this.width / 2,
                    58,
                    0xA0A0A0
            );
        } else {
            int centerX = this.width / 2;
            int pageSize = getPageSize();
            int start = this.page * pageSize;
            int end = Math.min(this.links.size(), start + pageSize);
            for (int index = start; index < end; index++) {
                BetterInputPayloads.BookLink link = this.links.get(index);
                int rowY = LIST_TOP + (index - start) * ROW_HEIGHT;
                context.drawTextWithShadow(
                        this.textRenderer,
                        Text.translatable(
                                "screen.betterinput.command_list.entry",
                                link.page() + 1,
                                trimCommand(link.command())
                        ),
                        centerX - 154,
                        rowY,
                        0xFFFFFF
                );
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private int getPageSize() {
        return Math.max(1, (this.height - LIST_TOP - 38) / ROW_HEIGHT);
    }

    private int getMaxPage(int pageSize) {
        if (this.links.isEmpty()) {
            return 0;
        }

        return (this.links.size() - 1) / pageSize;
    }

    private static String trimCommand(String command) {
        if (command.length() <= 28) {
            return command;
        }

        return command.substring(0, 25) + "...";
    }
}

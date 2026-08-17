package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputFormatting;
import com.daxia.betterinput.BetterInputBookLinkHolder;
import com.daxia.betterinput.BookCommandStorage;
import com.daxia.betterinput.BetterInputPayloads;
import com.daxia.betterinput.client.BookCommandListScreen;
import com.daxia.betterinput.client.FormatButtons;
import com.daxia.betterinput.client.LinkCommandScreen;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen implements BetterInputBookLinkHolder {
    @Shadow private EditBoxWidget editBox;
    @Shadow private int currentPage;
    @Shadow @Final private Hand hand;
    @Shadow @Final private ItemStack stack;
    @Shadow @Final private List<String> pages;
    @Unique private final List<BetterInputPayloads.BookLink> betterinput$links = new ArrayList<>();
    @Unique private boolean betterinput$loadedBookCommands;

    protected BookEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void betterinput$addToolbar(CallbackInfo ci) {
        betterinput$loadBookCommands();
        int toolbarX = Math.max(8, this.width - 158);
        for (var button : FormatButtons.create(toolbarX, 24, true, this::betterinput$formatSelection, this::betterinput$openLinkScreen)) {
            this.addDrawableChild(button);
        }
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.betterinput.command_list"),
                button -> betterinput$openCommandListScreen()
        ).dimensions(toolbarX + 74, 90, 70, 18).build());
    }

    @Inject(method = "finalizeBook", at = @At("TAIL"))
    private void betterinput$sendBookLinks(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        betterinput$refreshSelectedTexts();
        int slot = this.hand == Hand.OFF_HAND
                ? PlayerInventory.OFF_HAND_SLOT
                : client.player.getInventory().getSelectedSlot();
        ClientPlayNetworking.send(new BetterInputPayloads.BookLinks(slot, new ArrayList<>(this.betterinput$links), true));
    }

    @Unique
    @Override
    public List<BetterInputPayloads.BookLink> betterinput$getBookLinks() {
        return new ArrayList<>(this.betterinput$links);
    }

    @Unique
    private void betterinput$formatSelection(String code) {
        EditBox box = ((EditBoxWidgetAccessor) this.editBox).betterinput$getEditBox();
        String selected = box.getSelectedText();
        if (selected.isEmpty()) {
            box.replaceSelection(code);
            return;
        }

        box.replaceSelection(code + selected + BetterInputFormatting.RESET);
    }

    @Unique
    private void betterinput$openLinkScreen() {
        EditBox box = ((EditBoxWidgetAccessor) this.editBox).betterinput$getEditBox();
        String selected = box.getSelectedText();
        if (selected.isEmpty()) {
            return;
        }

        int page = this.currentPage;
        EditBoxAccessor accessor = (EditBoxAccessor) box;
        int start = Math.min(accessor.betterinput$getCursor(), accessor.betterinput$getSelectionEnd());
        this.client.setScreen(new LinkCommandScreen(this, command -> {
            String prefix = BetterInputFormatting.code('9') + BetterInputFormatting.code('n');
            String suffix = BetterInputFormatting.RESET;
            box.replaceSelection(prefix + selected + suffix);
            this.betterinput$links.add(new BetterInputPayloads.BookLink(
                    page,
                    start + prefix.length(),
                    start + prefix.length() + selected.length(),
                    selected,
                    command
            ));
        }));
    }

    @Unique
    private void betterinput$openCommandListScreen() {
        betterinput$refreshSelectedTexts();
        this.client.setScreen(new BookCommandListScreen(this, this.betterinput$links));
    }

    @Unique
    private void betterinput$loadBookCommands() {
        if (this.betterinput$loadedBookCommands) {
            return;
        }

        this.betterinput$loadedBookCommands = true;
        this.betterinput$links.clear();
        this.betterinput$links.addAll(BookCommandStorage.read(this.stack));
        betterinput$refreshSelectedTexts();
    }

    @Unique
    private void betterinput$refreshSelectedTexts() {
        for (int index = 0; index < this.betterinput$links.size(); index++) {
            BetterInputPayloads.BookLink link = this.betterinput$links.get(index);
            String selectedText = link.selectedText();
            if (selectedText == null || selectedText.isBlank()) {
                selectedText = betterinput$getLinkedText(link);
                this.betterinput$links.set(index, new BetterInputPayloads.BookLink(
                        link.page(),
                        link.start(),
                        link.end(),
                        selectedText,
                        link.command()
                ));
            }
        }
    }

    @Unique
    private String betterinput$getLinkedText(BetterInputPayloads.BookLink link) {
        if (link.page() < 0 || link.page() >= this.pages.size()) {
            return "";
        }

        String page = link.page() == this.currentPage && this.editBox != null
                ? this.editBox.getText()
                : this.pages.get(link.page());
        if (link.start() < 0 || link.end() > page.length() || link.start() >= link.end()) {
            return "";
        }

        return betterinput$stripFormattingCodes(page.substring(link.start(), link.end()));
    }

    @Unique
    private static String betterinput$stripFormattingCodes(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == Formatting.FORMATTING_CODE_PREFIX && index + 1 < text.length()) {
                Formatting formatting = Formatting.byCode(text.charAt(index + 1));
                if (formatting != null) {
                    index++;
                    continue;
                }
            }

            builder.append(text.charAt(index));
        }

        return builder.toString();
    }
}

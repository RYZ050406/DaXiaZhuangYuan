package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputFormatting;
import com.daxia.betterinput.BetterInputBookLinkHolder;
import com.daxia.betterinput.BetterInputPayloads;
import com.daxia.betterinput.client.FormatButtons;
import com.daxia.betterinput.client.LinkCommandScreen;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
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
    @Unique private final List<BetterInputPayloads.BookLink> betterinput$links = new ArrayList<>();

    protected BookEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void betterinput$addToolbar(CallbackInfo ci) {
        for (var button : FormatButtons.create(Math.max(8, this.width - 158), 24, true, this::betterinput$formatSelection, this::betterinput$openLinkScreen)) {
            this.addDrawableChild(button);
        }
    }

    @Inject(method = "finalizeBook", at = @At("TAIL"))
    private void betterinput$sendBookLinks(CallbackInfo ci) {
        if (this.betterinput$links.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        int slot = this.hand == Hand.OFF_HAND
                ? PlayerInventory.OFF_HAND_SLOT
                : client.player.getInventory().getSelectedSlot();
        ClientPlayNetworking.send(new BetterInputPayloads.BookLinks(slot, new ArrayList<>(this.betterinput$links)));
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
                    command
            ));
        }));
    }
}

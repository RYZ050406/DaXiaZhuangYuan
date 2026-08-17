package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputFormatting;
import com.daxia.betterinput.BetterInputPayloads;
import com.daxia.betterinput.client.FormatButtons;
import com.daxia.betterinput.client.LinkCommandScreen;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    @Shadow @Final protected SignBlockEntity blockEntity;
    @Shadow @Final private boolean front;
    @Shadow private SelectionManager selectionManager;
    @Shadow private String[] messages;
    @Shadow private int currentRow;
    @Unique private String betterinput$signCommand = "";
    @Unique private boolean betterinput$loadedSignCommand;
    @Unique private boolean betterinput$openingCommandScreen;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void betterinput$addToolbar(CallbackInfo ci) {
        betterinput$loadSignCommand();
        int toolbarX = Math.max(8, this.width - 158);
        for (var button : FormatButtons.create(toolbarX, 24, false, this::betterinput$formatSelection, () -> {
        })) {
            this.addDrawableChild(button);
        }
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.betterinput.sign_command"),
                button -> betterinput$openSignCommandScreen()
        ).dimensions(toolbarX, 90, 46, 18).build());
    }

    @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
    private void betterinput$sendSignFormatting(CallbackInfo ci) {
        if (this.betterinput$openingCommandScreen) {
            this.betterinput$openingCommandScreen = false;
            ci.cancel();
            return;
        }

        if (this.client == null || this.client.getNetworkHandler() == null) {
            return;
        }

        ClientPlayNetworking.send(new BetterInputPayloads.SignFormatting(
                this.blockEntity.getPos(),
                this.front,
                List.of(this.messages[0], this.messages[1], this.messages[2], this.messages[3]),
                this.betterinput$signCommand
        ));
    }

    @Unique
    private void betterinput$formatSelection(String code) {
        String current = this.messages[this.currentRow];
        int start = Math.min(this.selectionManager.getSelectionStart(), this.selectionManager.getSelectionEnd());
        int end = Math.max(this.selectionManager.getSelectionStart(), this.selectionManager.getSelectionEnd());
        if (start == end) {
            this.selectionManager.insert(code);
            return;
        }

        String selected = current.substring(start, end);
        this.selectionManager.insert(code + selected + BetterInputFormatting.RESET);
    }

    @Unique
    private void betterinput$openSignCommandScreen() {
        this.betterinput$openingCommandScreen = true;
        this.client.setScreen(new LinkCommandScreen(
                this,
                "screen.betterinput.sign_command.title",
                this.betterinput$signCommand,
                true,
                command -> this.betterinput$signCommand = command
        ));
    }

    @Unique
    private void betterinput$loadSignCommand() {
        if (this.betterinput$loadedSignCommand) {
            return;
        }

        this.betterinput$loadedSignCommand = true;
        SignText signText = this.blockEntity.getText(this.front);
        for (Text message : signText.getMessages(false)) {
            ClickEvent clickEvent = message.getStyle().getClickEvent();
            if (clickEvent instanceof ClickEvent.RunCommand runCommand) {
                this.betterinput$signCommand = betterinput$withLeadingSlash(runCommand.command());
                return;
            }
        }
    }

    @Unique
    private static String betterinput$withLeadingSlash(String command) {
        String trimmed = command.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("/")) {
            return trimmed;
        }

        return "/" + trimmed;
    }
}

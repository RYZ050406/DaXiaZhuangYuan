package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputFormatting;
import com.daxia.betterinput.client.FormatButtons;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    @Shadow private SelectionManager selectionManager;
    @Shadow private String[] messages;
    @Shadow private int currentRow;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void betterinput$addToolbar(CallbackInfo ci) {
        for (var button : FormatButtons.create(Math.max(8, this.width - 158), 24, false, this::betterinput$formatSelection, () -> {
        })) {
            this.addDrawableChild(button);
        }
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
}

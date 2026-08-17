package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputBookLinkHolder;
import com.daxia.betterinput.BetterInputPayloads;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.BookSigningScreen;
import net.minecraft.entity.player.PlayerEntity;
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

@Mixin(BookSigningScreen.class)
public abstract class BookSigningScreenMixin extends Screen {
    @Shadow @Final private BookEditScreen editScreen;
    @Shadow @Final private PlayerEntity player;
    @Shadow @Final private Hand hand;

    protected BookSigningScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "onFinalize", at = @At("TAIL"))
    private void betterinput$sendBookLinksAfterSigning(CallbackInfo ci) {
        List<BetterInputPayloads.BookLink> links = ((BetterInputBookLinkHolder) this.editScreen).betterinput$getBookLinks();
        if (links.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        ClientPlayNetworking.send(new BetterInputPayloads.BookLinks(this.betterinput$getBookSlot(), links));
    }

    @Unique
    private int betterinput$getBookSlot() {
        return this.hand == Hand.OFF_HAND
                ? PlayerInventory.OFF_HAND_SLOT
                : this.player.getInventory().getSelectedSlot();
    }
}

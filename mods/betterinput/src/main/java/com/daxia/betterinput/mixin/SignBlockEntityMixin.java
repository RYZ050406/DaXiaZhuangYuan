package com.daxia.betterinput.mixin;

import com.daxia.betterinput.BetterInputNetworking;
import com.daxia.betterinput.LegacyBookTextParser;
import java.util.List;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {
    @Shadow public abstract SignText getText(boolean front);

    @Shadow public abstract boolean setText(SignText text, boolean front);

    @Inject(method = "tryChangeText", at = @At("TAIL"))
    private void betterinput$applyFormattingToSavedSign(PlayerEntity player, boolean front, List<FilteredMessage> messages, CallbackInfo ci) {
        if (BetterInputNetworking.applyPendingSignFormatting(player, (SignBlockEntity) (Object) this, front)) {
            return;
        }

        SignText signText = this.getText(front);
        for (int index = 0; index < messages.size() && index < 4; index++) {
            FilteredMessage message = messages.get(index);
            Text rawText = LegacyBookTextParser.parse(message.raw(), List.of());
            Text filteredText = LegacyBookTextParser.parse(message.getString(), List.of());

            signText = player.shouldFilterText()
                    ? signText.withMessage(index, filteredText)
                    : signText.withMessage(index, rawText, filteredText);
        }

        this.setText(signText, front);
    }
}

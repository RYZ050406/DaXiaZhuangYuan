package com.daxia.betterinput.mixin;

import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringHelper.class)
public abstract class StringHelperMixin {
    @Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
    private static void betterinput$allowFormattingPrefix(int character, CallbackInfoReturnable<Boolean> cir) {
        if (character == Formatting.FORMATTING_CODE_PREFIX) {
            cir.setReturnValue(true);
        }
    }
}

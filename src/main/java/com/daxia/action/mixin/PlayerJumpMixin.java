package com.daxia.action.mixin;

import com.daxia.action.ActionStateStore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerJumpMixin {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void action$blockJumpForLockedActions(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity && ActionStateStore.get(entity).movementScale() == 0.0D) {
            ci.cancel();
        }
    }
}

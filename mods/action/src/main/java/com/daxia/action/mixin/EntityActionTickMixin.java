package com.daxia.action.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityActionTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void action$refreshActionBox(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity && ActionStateStore.get(entity) != ActionType.NONE) {
            entity.calculateDimensions();
        }
    }
}

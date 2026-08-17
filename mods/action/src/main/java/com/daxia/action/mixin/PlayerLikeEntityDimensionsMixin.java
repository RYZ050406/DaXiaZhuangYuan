package com.daxia.action.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerLikeEntity.class)
public abstract class PlayerLikeEntityDimensionsMixin {
    @Inject(method = "getBaseDimensions", at = @At("RETURN"), cancellable = true)
    private void action$useActionDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        PlayerLikeEntity entity = (PlayerLikeEntity) (Object) this;
        ActionType action = ActionStateStore.get(entity);
        EntityDimensions dimensions = action.dimensionsOrNull();
        if (dimensions != null) {
            cir.setReturnValue(dimensions);
        }
    }
}

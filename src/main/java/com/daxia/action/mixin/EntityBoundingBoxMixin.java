package com.daxia.action.mixin;

import com.daxia.action.ActionCollision;
import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityBoundingBoxMixin {
    @Inject(method = "calculateDefaultBoundingBox", at = @At("HEAD"), cancellable = true)
    private void action$calculateActionBoundingBox(Vec3d pos, CallbackInfoReturnable<Box> cir) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        ActionType action = ActionStateStore.get(entity);
        if (action != ActionType.NONE) {
            cir.setReturnValue(ActionCollision.createActionBox(entity, pos, action));
        }
    }
}

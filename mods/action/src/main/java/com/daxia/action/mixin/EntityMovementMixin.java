package com.daxia.action.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMovementMixin {
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3d action$scalePlayerMovement(Vec3d movement, MovementType movementType) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PlayerEntity)) {
            return movement;
        }

        ActionType action = ActionStateStore.get(entity);
        double scale = action.movementScale();
        if (scale >= 1.0D) {
            return movement;
        }

        return new Vec3d(movement.x * scale, movement.y, movement.z * scale);
    }
}

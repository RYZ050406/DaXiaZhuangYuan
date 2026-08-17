package com.daxia.action.client.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Entity focusedEntity;
    @Shadow private float cameraY;
    @Shadow private float lastCameraY;

    @Inject(method = "updateEyeHeight", at = @At("TAIL"))
    private void action$snapCameraToActionEyeHeight(CallbackInfo ci) {
        if (focusedEntity == null) {
            return;
        }

        ActionType action = ActionStateStore.get(focusedEntity);
        if (action == ActionType.LIE || action == ActionType.PRONE) {
            this.cameraY = action.eyeHeight();
            this.lastCameraY = action.eyeHeight();
        }
    }
}

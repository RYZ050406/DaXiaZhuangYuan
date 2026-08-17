package com.daxia.action.client.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(
            method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
            at = @At("TAIL")
    )
    private void action$setupActionTransforms(PlayerEntityRenderState state, MatrixStack matrices, float animationProgress, float bodyYaw, CallbackInfo ci) {
        ActionType action = ActionStateStore.get(state.id);

        switch (action) {
            case SIT -> matrices.translate(0.0D, -0.22D, 0.0D);
            case LIE -> {
                matrices.translate(0.0D, 0.38D, 0.34D);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            }
            case PRONE -> {
                matrices.translate(0.0D, 0.32D, -0.18D);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            }
            case NONE, RAISE_RIGHT, RAISE_LEFT, WAVE_RIGHT, WAVE_LEFT -> {
            }
        }
    }
}

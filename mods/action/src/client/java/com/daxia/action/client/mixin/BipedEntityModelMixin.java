package com.daxia.action.client.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import com.daxia.action.client.ActionPoseApplier;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
    private void action$applyPlayerActionPoseToBipedLayers(BipedEntityRenderState state, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        ActionType action = ActionStateStore.get(playerState.id);
        if (action != ActionType.NONE) {
            BipedEntityModel<?> model = (BipedEntityModel<?>) (Object) this;
            ActionPoseApplier.apply(model, action, playerState.age);
        }
    }
}

package com.daxia.action.client.mixin;

import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import com.daxia.action.client.ActionPoseApplier;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin extends BipedEntityModel<PlayerEntityRenderState> {
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftPants;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart jacket;

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void action$applyActionPose(PlayerEntityRenderState state, CallbackInfo ci) {
        ActionType action = ActionStateStore.get(state.id);
        if (action == ActionType.NONE) {
            return;
        }

        ActionPoseApplier.apply(this, action, state.age);
        ActionPoseApplier.copyPart(this.rightArm, this.rightSleeve);
        ActionPoseApplier.copyPart(this.leftArm, this.leftSleeve);
        ActionPoseApplier.copyPart(this.rightLeg, this.rightPants);
        ActionPoseApplier.copyPart(this.leftLeg, this.leftPants);
        ActionPoseApplier.copyPart(this.body, this.jacket);
    }
}

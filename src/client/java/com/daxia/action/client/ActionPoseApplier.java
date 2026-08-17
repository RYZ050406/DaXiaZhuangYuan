package com.daxia.action.client;

import com.daxia.action.ActionType;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;

public final class ActionPoseApplier {
    private ActionPoseApplier() {
    }

    public static void apply(BipedEntityModel<?> model, ActionType action, float age) {
        float wave = MathHelper.sin(age * 0.55F) * 0.7F;

        switch (action) {
            case RAISE_RIGHT -> {
                model.rightArm.pitch = -2.95F;
                model.rightArm.yaw = -0.18F;
                model.rightArm.roll = 0.18F;
            }
            case RAISE_LEFT -> {
                model.leftArm.pitch = -2.95F;
                model.leftArm.yaw = 0.18F;
                model.leftArm.roll = -0.18F;
            }
            case WAVE_RIGHT -> {
                model.rightArm.pitch = -2.35F;
                model.rightArm.yaw = -0.45F + wave * 0.35F;
                model.rightArm.roll = 0.55F + wave;
            }
            case WAVE_LEFT -> {
                model.leftArm.pitch = -2.35F;
                model.leftArm.yaw = 0.45F - wave * 0.35F;
                model.leftArm.roll = -0.55F - wave;
            }
            case SIT -> applySitPose(model);
            case LIE -> applyLiePose(model);
            case PRONE -> applyPronePose(model);
            case NONE -> {
            }
        }
    }

    public static void copyPart(ModelPart source, ModelPart target) {
        target.originX = source.originX;
        target.originY = source.originY;
        target.originZ = source.originZ;
        target.pitch = source.pitch;
        target.yaw = source.yaw;
        target.roll = source.roll;
        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;
        target.visible = source.visible;
    }

    private static void applySitPose(BipedEntityModel<?> model) {
        model.body.pitch = 0.15F;
        model.rightArm.pitch = -0.35F;
        model.leftArm.pitch = -0.35F;
        model.rightLeg.pitch = -1.45F;
        model.leftLeg.pitch = -1.45F;
        model.rightLeg.yaw = 0.25F;
        model.leftLeg.yaw = -0.25F;
    }

    private static void applyLiePose(BipedEntityModel<?> model) {
        model.head.pitch = 0.0F;
        model.body.pitch = 0.0F;
        model.rightArm.pitch = -0.25F;
        model.leftArm.pitch = -0.25F;
        model.rightLeg.pitch = 0.05F;
        model.leftLeg.pitch = 0.05F;
    }

    private static void applyPronePose(BipedEntityModel<?> model) {
        model.head.pitch = 0.35F;
        model.body.pitch = 0.08F;
        model.rightArm.pitch = -2.75F;
        model.leftArm.pitch = -2.75F;
        model.rightArm.roll = 0.18F;
        model.leftArm.roll = -0.18F;
        model.rightLeg.pitch = 0.12F;
        model.leftLeg.pitch = -0.12F;
    }
}

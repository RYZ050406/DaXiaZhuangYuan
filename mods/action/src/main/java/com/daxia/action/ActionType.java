package com.daxia.action;

import java.util.Arrays;
import net.minecraft.entity.EntityDimensions;

public enum ActionType {
    NONE(0, "none", 1.0D, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
    RAISE_RIGHT(1, "raise_right", 1.0D, 0.78F, 0.78F, 2.15F, 1.62F, 0.10F),
    RAISE_LEFT(2, "raise_left", 1.0D, 0.78F, 0.78F, 2.15F, 1.62F, -0.10F),
    WAVE_RIGHT(3, "wave_right", 1.0D, 0.92F, 0.92F, 2.05F, 1.62F, 0.12F),
    WAVE_LEFT(4, "wave_left", 1.0D, 0.92F, 0.92F, 2.05F, 1.62F, -0.12F),
    SIT(5, "sit", 0.0D, 0.72F, 0.72F, 0.95F, 0.65F, 0.0F),
    LIE(6, "lie", 0.2D, 0.58F, 1.85F, 0.35F, 0.22F, 0.0F),
    PRONE(7, "prone", 0.4D, 0.72F, 1.55F, 0.55F, 0.30F, 0.0F);

    private static final ActionType[] MENU_ACTIONS = Arrays.stream(values())
            .filter(action -> action != NONE)
            .toArray(ActionType[]::new);

    private final int networkId;
    private final String path;
    private final double movementScale;
    private final float collisionWidth;
    private final float collisionDepth;
    private final float collisionHeight;
    private final float eyeHeight;
    private final float rightOffset;

    ActionType(int networkId, String path, double movementScale, float collisionWidth, float collisionDepth, float collisionHeight, float eyeHeight, float rightOffset) {
        this.networkId = networkId;
        this.path = path;
        this.movementScale = movementScale;
        this.collisionWidth = collisionWidth;
        this.collisionDepth = collisionDepth;
        this.collisionHeight = collisionHeight;
        this.eyeHeight = eyeHeight;
        this.rightOffset = rightOffset;
    }

    public int networkId() {
        return networkId;
    }

    public String translationKey() {
        return "action.action." + path;
    }

    public double movementScale() {
        return movementScale;
    }

    public float collisionWidth() {
        return collisionWidth;
    }

    public float collisionDepth() {
        return collisionDepth;
    }

    public float collisionHeight() {
        return collisionHeight;
    }

    public float eyeHeight() {
        return eyeHeight;
    }

    public float rightOffset() {
        return rightOffset;
    }

    public EntityDimensions dimensionsOrNull() {
        if (this == NONE) {
            return null;
        }

        return EntityDimensions.changing(Math.max(collisionWidth, collisionDepth), collisionHeight)
                .withEyeHeight(eyeHeight);
    }

    public static ActionType fromNetworkId(int networkId) {
        for (ActionType action : values()) {
            if (action.networkId == networkId) {
                return action;
            }
        }

        return NONE;
    }

    public static ActionType[] menuActions() {
        return MENU_ACTIONS.clone();
    }
}

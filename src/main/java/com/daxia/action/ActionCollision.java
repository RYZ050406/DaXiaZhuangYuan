package com.daxia.action;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ActionCollision {
    private ActionCollision() {
    }

    public static Box createActionBox(Entity entity, Vec3d pos, ActionType action) {
        double yaw = Math.toRadians(entity.getYaw());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        double centerX = pos.x + rightX * action.rightOffset();
        double centerZ = pos.z + rightZ * action.rightOffset();
        double halfX = Math.abs(rightX) * action.collisionWidth() * 0.5D
                + Math.abs(forwardX) * action.collisionDepth() * 0.5D;
        double halfZ = Math.abs(rightZ) * action.collisionWidth() * 0.5D
                + Math.abs(forwardZ) * action.collisionDepth() * 0.5D;

        return new Box(
                centerX - halfX,
                pos.y,
                centerZ - halfZ,
                centerX + halfX,
                pos.y + action.collisionHeight(),
                centerZ + halfZ
        );
    }
}

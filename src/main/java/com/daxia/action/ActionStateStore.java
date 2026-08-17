package com.daxia.action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;

public final class ActionStateStore {
    private static final Map<Integer, ActionType> ACTIONS_BY_ENTITY_ID = new ConcurrentHashMap<>();

    private ActionStateStore() {
    }

    public static ActionType get(Entity entity) {
        return get(entity.getId());
    }

    public static ActionType get(int entityId) {
        return ACTIONS_BY_ENTITY_ID.getOrDefault(entityId, ActionType.NONE);
    }

    public static void set(Entity entity, ActionType action) {
        set(entity.getId(), action);
    }

    public static void set(int entityId, ActionType action) {
        if (action == ActionType.NONE) {
            ACTIONS_BY_ENTITY_ID.remove(entityId);
            return;
        }

        ACTIONS_BY_ENTITY_ID.put(entityId, action);
    }

    public static void clear(Entity entity) {
        ACTIONS_BY_ENTITY_ID.remove(entity.getId());
    }
}

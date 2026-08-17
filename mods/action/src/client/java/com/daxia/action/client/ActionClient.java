package com.daxia.action.client;

import com.daxia.action.ActionMod;
import com.daxia.action.ActionPayloads;
import com.daxia.action.ActionStateStore;
import com.daxia.action.ActionType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public final class ActionClient implements ClientModInitializer {
    private static KeyBinding openPanelKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(ActionMod.id("controls"));
        openPanelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.action.open_panel",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPanelKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new ActionPanelScreen());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ActionPayloads.Sync.ID, (payload, context) ->
                context.client().execute(() -> applySyncedAction(payload.entityId(), payload.actionId()))
        );
    }

    public static void requestAction(ActionType action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            ClientPlayNetworking.send(new ActionPayloads.Select(action.networkId()));
        }
    }

    private static void applySyncedAction(int entityId, int actionId) {
        ActionType action = ActionType.fromNetworkId(actionId);
        ActionStateStore.set(entityId, action);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        Entity entity = client.world.getEntityById(entityId);
        if (entity != null) {
            entity.calculateDimensions();
        }
    }
}

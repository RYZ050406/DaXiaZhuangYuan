package com.daxia.action;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ActionNetworking {
    private ActionNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ActionPayloads.Select.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    ActionType action = ActionType.fromNetworkId(payload.actionId());
                    ActionStateStore.set(player, action);
                    player.calculateDimensions();
                    syncToAll(player, context.server());
                })
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> syncExistingActionsTo(handler.player, server))
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                server.execute(() -> {
                    int entityId = handler.player.getId();
                    ActionStateStore.clear(handler.player);
                    sendToAll(server, new ActionPayloads.Sync(entityId, ActionType.NONE.networkId()));
                })
        );
    }

    private static void syncToAll(ServerPlayerEntity player, MinecraftServer server) {
        sendToAll(server, new ActionPayloads.Sync(
                player.getId(),
                ActionStateStore.get(player).networkId()
        ));
    }

    private static void syncExistingActionsTo(ServerPlayerEntity receiver, MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ActionType action = ActionStateStore.get(player);
            if (action != ActionType.NONE) {
                ServerPlayNetworking.send(receiver, new ActionPayloads.Sync(player.getId(), action.networkId()));
            }
        }
    }

    private static void sendToAll(MinecraftServer server, ActionPayloads.Sync payload) {
        for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(target, payload);
        }
    }
}

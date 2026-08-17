package com.daxia.action;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public final class ActionPayloads {
    private ActionPayloads() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(Select.ID, Select.CODEC);
        PayloadTypeRegistry.playS2C().register(Sync.ID, Sync.CODEC);
    }

    public record Select(int actionId) implements CustomPayload {
        public static final CustomPayload.Id<Select> ID = new CustomPayload.Id<>(ActionMod.id("select_action"));
        public static final PacketCodec<RegistryByteBuf, Select> CODEC = PacketCodec.ofStatic(
                (buffer, payload) -> buffer.writeVarInt(payload.actionId),
                buffer -> new Select(buffer.readVarInt())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record Sync(int entityId, int actionId) implements CustomPayload {
        public static final CustomPayload.Id<Sync> ID = new CustomPayload.Id<>(ActionMod.id("sync_action"));
        public static final PacketCodec<RegistryByteBuf, Sync> CODEC = PacketCodec.ofStatic(
                (buffer, payload) -> {
                    buffer.writeVarInt(payload.entityId);
                    buffer.writeVarInt(payload.actionId);
                },
                buffer -> new Sync(buffer.readVarInt(), buffer.readVarInt())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

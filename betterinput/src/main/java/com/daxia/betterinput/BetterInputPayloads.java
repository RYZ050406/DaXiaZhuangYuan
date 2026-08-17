package com.daxia.betterinput;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public final class BetterInputPayloads {
    private BetterInputPayloads() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(BookLinks.ID, BookLinks.CODEC);
    }

    public record BookLink(int page, int start, int end, String command) {
    }

    public record BookLinks(int slot, List<BookLink> links) implements CustomPayload {
        public static final CustomPayload.Id<BookLinks> ID = new CustomPayload.Id<>(BetterInputMod.id("book_links"));
        public static final PacketCodec<RegistryByteBuf, BookLinks> CODEC = PacketCodec.ofStatic(
                (buffer, payload) -> {
                    buffer.writeVarInt(payload.slot);
                    buffer.writeVarInt(payload.links.size());
                    for (BookLink link : payload.links) {
                        buffer.writeVarInt(link.page());
                        buffer.writeVarInt(link.start());
                        buffer.writeVarInt(link.end());
                        buffer.writeString(link.command(), 256);
                    }
                },
                buffer -> {
                    int slot = buffer.readVarInt();
                    int size = buffer.readVarInt();
                    List<BookLink> links = new ArrayList<>(size);
                    for (int index = 0; index < size; index++) {
                        links.add(new BookLink(
                                buffer.readVarInt(),
                                buffer.readVarInt(),
                                buffer.readVarInt(),
                                buffer.readString(256)
                        ));
                    }
                    return new BookLinks(slot, links);
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

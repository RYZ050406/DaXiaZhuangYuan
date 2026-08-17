package com.daxia.betterinput;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public final class BetterInputPayloads {
    private BetterInputPayloads() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(BookLinks.ID, BookLinks.CODEC);
        PayloadTypeRegistry.playC2S().register(SignFormatting.ID, SignFormatting.CODEC);
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

    public record SignFormatting(BlockPos pos, boolean front, List<String> lines, String command) implements CustomPayload {
        public static final CustomPayload.Id<SignFormatting> ID = new CustomPayload.Id<>(BetterInputMod.id("sign_formatting"));
        public static final PacketCodec<RegistryByteBuf, SignFormatting> CODEC = PacketCodec.ofStatic(
                (buffer, payload) -> {
                    buffer.writeBlockPos(payload.pos);
                    buffer.writeBoolean(payload.front);
                    for (int index = 0; index < 4; index++) {
                        String line = index < payload.lines.size() ? payload.lines.get(index) : "";
                        buffer.writeString(line, 384);
                    }
                    buffer.writeString(payload.command, 256);
                },
                buffer -> {
                    BlockPos pos = buffer.readBlockPos();
                    boolean front = buffer.readBoolean();
                    List<String> lines = new ArrayList<>(4);
                    for (int index = 0; index < 4; index++) {
                        lines.add(buffer.readString(384));
                    }
                    return new SignFormatting(pos, front, lines, buffer.readString(256));
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

package com.daxia.betterinput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

public final class BetterInputNetworking {
    private static final int MAX_PENDING_TICKS = 200;
    private static final Map<PendingKey, PendingBookLinks> PENDING_BOOK_LINKS = new HashMap<>();
    private static final Map<PendingSignKey, PendingSignFormatting> PENDING_SIGN_FORMATTING = new HashMap<>();

    private BetterInputNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(BetterInputPayloads.BookLinks.ID, (payload, context) ->
                context.server().execute(() -> queueBookLinks(context.player(), payload))
        );
        ServerPlayNetworking.registerGlobalReceiver(BetterInputPayloads.SignFormatting.ID, (payload, context) ->
                context.server().execute(() -> queueSignFormatting(context.player(), payload))
        );
        ServerTickEvents.END_SERVER_TICK.register(BetterInputNetworking::processPendingBookLinks);
    }

    private static void queueBookLinks(ServerPlayerEntity player, BetterInputPayloads.BookLinks payload) {
        BetterInputPayloads.BookLinks copiedPayload = new BetterInputPayloads.BookLinks(
                payload.slot(),
                new ArrayList<>(payload.links()),
                payload.storeWritable()
        );
        if (tryApplyBookLinks(player, copiedPayload)) {
            return;
        }

        PENDING_BOOK_LINKS.put(new PendingKey(player.getUuid(), payload.slot()), new PendingBookLinks(copiedPayload));
    }

    private static void processPendingBookLinks(MinecraftServer server) {
        Iterator<Map.Entry<PendingKey, PendingBookLinks>> iterator = PENDING_BOOK_LINKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PendingKey, PendingBookLinks> entry = iterator.next();
            PendingKey key = entry.getKey();
            PendingBookLinks pending = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(key.playerUuid());

            pending.age++;
            if (player == null || pending.age > MAX_PENDING_TICKS) {
                iterator.remove();
                continue;
            }

            if (tryApplyBookLinks(player, pending.payload)) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<PendingSignKey, PendingSignFormatting>> signIterator = PENDING_SIGN_FORMATTING.entrySet().iterator();
        while (signIterator.hasNext()) {
            Map.Entry<PendingSignKey, PendingSignFormatting> entry = signIterator.next();
            PendingSignFormatting pending = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey().playerUuid());

            pending.age++;
            if (player == null || pending.age > MAX_PENDING_TICKS) {
                signIterator.remove();
                continue;
            }

            if (pending.age >= 10 && tryApplySignFormatting(player, pending.payload)) {
                signIterator.remove();
            }
        }
    }

    private static boolean tryApplyBookLinks(ServerPlayerEntity player, BetterInputPayloads.BookLinks payload) {
        ItemStack stack = player.getInventory().getStack(payload.slot());
        if (payload.storeWritable() && stack.isOf(Items.WRITABLE_BOOK)) {
            BookCommandStorage.write(stack, payload.links());
            player.getInventory().setStack(payload.slot(), stack);
            player.getInventory().markDirty();
            player.playerScreenHandler.sendContentUpdates();
            if (player.currentScreenHandler != player.playerScreenHandler) {
                player.currentScreenHandler.sendContentUpdates();
            }
            return true;
        }

        if (!stack.isOf(Items.WRITTEN_BOOK)) {
            return false;
        }

        WrittenBookContentComponent content = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            return false;
        }

        List<RawFilteredPair<Text>> pages = content.pages();
        List<RawFilteredPair<Text>> newPages = new ArrayList<>(pages.size());
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            int currentPageIndex = pageIndex;
            String rawPage = pages.get(pageIndex).raw().getString();
            List<BetterInputPayloads.BookLink> pageLinks = payload.links().stream()
                    .filter(link -> link.page() == currentPageIndex)
                    .sorted(Comparator.comparingInt(BetterInputPayloads.BookLink::start))
                    .toList();
            newPages.add(RawFilteredPair.of(LegacyBookTextParser.parse(rawPage, pageLinks)));
        }

        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                content.title(),
                content.author(),
                content.generation(),
                newPages,
                true
        ));
        player.getInventory().setStack(payload.slot(), stack);
        player.getInventory().markDirty();
        player.playerScreenHandler.sendContentUpdates();
        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.currentScreenHandler.sendContentUpdates();
        }
        return true;
    }

    private static void queueSignFormatting(ServerPlayerEntity player, BetterInputPayloads.SignFormatting payload) {
        PENDING_SIGN_FORMATTING.put(
                new PendingSignKey(player.getUuid(), payload.pos(), payload.front()),
                new PendingSignFormatting(new BetterInputPayloads.SignFormatting(
                        payload.pos(),
                        payload.front(),
                        new ArrayList<>(payload.lines()),
                        payload.command()
                ))
        );
    }

    public static boolean applyPendingSignFormatting(PlayerEntity player, SignBlockEntity sign, boolean front) {
        PendingSignKey key = new PendingSignKey(player.getUuid(), sign.getPos(), front);
        PendingSignFormatting pending = PENDING_SIGN_FORMATTING.remove(key);
        if (pending == null) {
            return false;
        }

        return applySignFormatting(sign, front, pending.payload.lines(), pending.payload.command());
    }

    private static boolean tryApplySignFormatting(ServerPlayerEntity player, BetterInputPayloads.SignFormatting payload) {
        ServerWorld world = player.getEntityWorld();
        if (!(world.getBlockEntity(payload.pos()) instanceof SignBlockEntity sign)) {
            return false;
        }

        return applySignFormatting(sign, payload.front(), payload.lines(), payload.command());
    }

    private static boolean applySignFormatting(SignBlockEntity sign, boolean front, List<String> lines, String command) {
        SignText signText = sign.getText(front);
        String signCommand = normalizeSignCommand(command);
        for (int index = 0; index < lines.size() && index < 4; index++) {
            Text parsed = LegacyBookTextParser.parse(lines.get(index), List.of());
            if (index == 0 && !signCommand.isEmpty()) {
                parsed = parsed.copy().setStyle(parsed.getStyle().withClickEvent(new ClickEvent.RunCommand(signCommand)));
            }
            signText = signText.withMessage(index, parsed, parsed);
        }

        return sign.setText(signText, front);
    }

    private static String normalizeSignCommand(String command) {
        String trimmed = command.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1).trim();
        }

        return trimmed;
    }

    private record PendingKey(UUID playerUuid, int slot) {
    }

    private record PendingSignKey(UUID playerUuid, net.minecraft.util.math.BlockPos pos, boolean front) {
    }

    private static final class PendingBookLinks {
        private final BetterInputPayloads.BookLinks payload;
        private int age;

        private PendingBookLinks(BetterInputPayloads.BookLinks payload) {
            this.payload = payload;
        }
    }

    private static final class PendingSignFormatting {
        private final BetterInputPayloads.SignFormatting payload;
        private int age;

        private PendingSignFormatting(BetterInputPayloads.SignFormatting payload) {
            this.payload = payload;
        }
    }
}

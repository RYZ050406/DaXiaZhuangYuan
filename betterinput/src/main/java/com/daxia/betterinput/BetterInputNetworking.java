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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

public final class BetterInputNetworking {
    private static final int MAX_PENDING_TICKS = 200;
    private static final Map<PendingKey, PendingBookLinks> PENDING_BOOK_LINKS = new HashMap<>();

    private BetterInputNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(BetterInputPayloads.BookLinks.ID, (payload, context) ->
                context.server().execute(() -> queueBookLinks(context.player(), payload))
        );
        ServerTickEvents.END_SERVER_TICK.register(BetterInputNetworking::processPendingBookLinks);
    }

    private static void queueBookLinks(ServerPlayerEntity player, BetterInputPayloads.BookLinks payload) {
        if (payload.links().isEmpty()) {
            return;
        }

        BetterInputPayloads.BookLinks copiedPayload = new BetterInputPayloads.BookLinks(
                payload.slot(),
                new ArrayList<>(payload.links())
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
    }

    private static boolean tryApplyBookLinks(ServerPlayerEntity player, BetterInputPayloads.BookLinks payload) {
        ItemStack stack = player.getInventory().getStack(payload.slot());
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

    private record PendingKey(UUID playerUuid, int slot) {
    }

    private static final class PendingBookLinks {
        private final BetterInputPayloads.BookLinks payload;
        private int age;

        private PendingBookLinks(BetterInputPayloads.BookLinks payload) {
            this.payload = payload;
        }
    }
}

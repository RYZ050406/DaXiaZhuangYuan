package com.daxia.betterinput;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public final class BookCommandStorage {
    private static final String ROOT_KEY = BetterInputMod.MOD_ID;
    private static final String LINKS_KEY = "book_commands";
    private static final String PAGE_KEY = "page";
    private static final String START_KEY = "start";
    private static final String END_KEY = "end";
    private static final String TEXT_KEY = "text";
    private static final String COMMAND_KEY = "command";

    private BookCommandStorage() {
    }

    public static List<BetterInputPayloads.BookLink> read(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return List.of();
        }

        NbtCompound root = customData.copyNbt().getCompoundOrEmpty(ROOT_KEY);
        NbtList storedLinks = root.getListOrEmpty(LINKS_KEY);
        List<BetterInputPayloads.BookLink> links = new ArrayList<>(storedLinks.size());
        for (int index = 0; index < storedLinks.size(); index++) {
            NbtCompound link = storedLinks.getCompoundOrEmpty(index);
            String command = link.getString(COMMAND_KEY, "").trim();
            if (command.isEmpty()) {
                continue;
            }

            links.add(new BetterInputPayloads.BookLink(
                    link.getInt(PAGE_KEY, 0),
                    link.getInt(START_KEY, 0),
                    link.getInt(END_KEY, 0),
                    link.getString(TEXT_KEY, ""),
                    command
            ));
        }

        return links;
    }

    public static void write(ItemStack stack, List<BetterInputPayloads.BookLink> links) {
        NbtCompound customData = stack.get(DataComponentTypes.CUSTOM_DATA) == null
                ? new NbtCompound()
                : stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();

        if (links.isEmpty()) {
            NbtCompound root = customData.getCompoundOrEmpty(ROOT_KEY);
            root.remove(LINKS_KEY);
            if (root.isEmpty()) {
                customData.remove(ROOT_KEY);
            } else {
                customData.put(ROOT_KEY, root);
            }
        } else {
            NbtCompound root = customData.getCompoundOrEmpty(ROOT_KEY);
            NbtList storedLinks = new NbtList();
            for (BetterInputPayloads.BookLink link : links) {
                String command = link.command().trim();
                if (command.isEmpty()) {
                    continue;
                }

                NbtCompound storedLink = new NbtCompound();
                storedLink.putInt(PAGE_KEY, link.page());
                storedLink.putInt(START_KEY, link.start());
                storedLink.putInt(END_KEY, link.end());
                storedLink.putString(TEXT_KEY, link.selectedText());
                storedLink.putString(COMMAND_KEY, command);
                storedLinks.add(storedLink);
            }
            root.put(LINKS_KEY, storedLinks);
            customData.put(ROOT_KEY, root);
        }

        if (customData.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        }
    }
}

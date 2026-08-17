package com.daxia.betterinput;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BetterInputMod implements ModInitializer {
    public static final String MOD_ID = "betterinput";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        BetterInputPayloads.registerPayloadTypes();
        BetterInputNetworking.registerServerReceivers();
    }
}

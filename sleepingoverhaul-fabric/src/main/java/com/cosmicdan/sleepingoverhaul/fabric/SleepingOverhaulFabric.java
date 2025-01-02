package com.cosmicdan.sleepingoverhaul.fabric;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;

public class SleepingOverhaulFabric implements ModInitializer {
    private SleepingOverhaul instance = null;

    @Override
    public void onInitialize() {
        instance = new SleepingOverhaul();

        /**
         * Only used for performance counting stats right now
         */
        ServerTickEvents.END_SERVER_TICK.register((final MinecraftServer server) -> {
            SleepingOverhaul.serverState.onServerTickPost();
        });
    }
}

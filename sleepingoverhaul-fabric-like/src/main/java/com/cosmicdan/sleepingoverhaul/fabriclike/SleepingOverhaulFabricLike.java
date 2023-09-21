package com.cosmicdan.sleepingoverhaul.fabriclike;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class SleepingOverhaulFabricLike {
    private final SleepingOverhaul INSTANCE;

    public SleepingOverhaulFabricLike() {
        INSTANCE = new SleepingOverhaul();

        /**
         * Only used for performance counting stats right now
         */
        ServerTickEvents.END_SERVER_TICK.register((final MinecraftServer server) -> {
            SleepingOverhaul.serverState.onServerTickPost();
        });

        // TODO: Fabric has a crap load of EntitySleepEvents, maybe many common mixins are only necessary for Forge...?
    }
}

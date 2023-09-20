package com.cosmicdan.sleepingoverhaul.fabric;

import com.cosmicdan.sleepingoverhaul.fabriclike.SleepingOverhaulFabricLike;
import net.fabricmc.api.ModInitializer;

public class SleepingOverhaulFabric implements ModInitializer {
    private SleepingOverhaulFabricLike instance = null;

    @Override
    public void onInitialize() {
        instance = new SleepingOverhaulFabricLike();
    }
}

package com.cosmicdan.sleepingoverhaul.fabric;

import com.cosmicdan.sleepingoverhaul.fabriclike.SleepingOverhaulFabricLike;
import net.fabricmc.api.ModInitializer;

public class SleepingOverhaulFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SleepingOverhaulFabricLike.init();
    }
}

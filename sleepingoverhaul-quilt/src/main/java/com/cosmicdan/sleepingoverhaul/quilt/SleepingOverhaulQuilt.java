package com.cosmicdan.sleepingoverhaul.quilt;

import com.cosmicdan.sleepingoverhaul.fabriclike.SleepingOverhaulFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class SleepingOverhaulQuilt implements ModInitializer {
    private SleepingOverhaulFabricLike instance = null;

    @Override
    public void onInitialize(ModContainer mod) {
        instance = new SleepingOverhaulFabricLike();
    }
}

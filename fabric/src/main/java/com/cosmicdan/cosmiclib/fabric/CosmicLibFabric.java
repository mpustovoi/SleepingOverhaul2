package com.cosmicdan.cosmiclib.fabric;

import com.cosmicdan.cosmiclib.fabriclike.CosmicLibFabricLike;
import net.fabricmc.api.ModInitializer;

public class CosmicLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CosmicLibFabricLike.init();
    }
}

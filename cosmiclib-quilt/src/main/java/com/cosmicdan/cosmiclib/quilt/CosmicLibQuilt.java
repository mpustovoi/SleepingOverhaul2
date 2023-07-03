package com.cosmicdan.cosmiclib.quilt;

import com.cosmicdan.cosmiclib.fabriclike.CosmicLibFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class CosmicLibQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        CosmicLibFabricLike.init();
    }
}

package com.cosmicdan.cosmiclib.forge;

import dev.architectury.platform.forge.EventBuses;
import com.cosmicdan.cosmiclib.CosmicLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CosmicLib.MOD_ID)
public class CosmicLibForge {
    public CosmicLibForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(CosmicLib.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CosmicLib.init();
    }
}

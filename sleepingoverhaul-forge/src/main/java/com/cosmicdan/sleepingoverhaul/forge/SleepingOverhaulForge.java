package com.cosmicdan.sleepingoverhaul.forge;

import dev.architectury.platform.forge.EventBuses;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SleepingOverhaul.MOD_ID)
public class SleepingOverhaulForge {
    public SleepingOverhaulForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(SleepingOverhaul.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SleepingOverhaul.init();
    }
}

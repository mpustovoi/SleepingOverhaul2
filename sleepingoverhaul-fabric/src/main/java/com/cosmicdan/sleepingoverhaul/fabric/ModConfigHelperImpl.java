package com.cosmicdan.sleepingoverhaul.fabric;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModConfigHelperImpl {
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {
        ModLoadingContext.registerConfig(SleepingOverhaul.MOD_ID, type, spec);
    }
}

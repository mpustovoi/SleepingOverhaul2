package com.cosmicdan.sleepingoverhaul.forge;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModConfigHelperImpl {
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {
        ModLoadingContext.get().registerConfig(type, spec);
    }
}

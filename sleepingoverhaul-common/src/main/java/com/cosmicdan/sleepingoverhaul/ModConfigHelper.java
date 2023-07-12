package com.cosmicdan.sleepingoverhaul;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModConfigHelper {
    @ExpectPlatform
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {
        throw new AssertionError();
    }

}

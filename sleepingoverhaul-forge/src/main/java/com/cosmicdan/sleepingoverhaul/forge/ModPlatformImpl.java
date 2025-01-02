package com.cosmicdan.sleepingoverhaul.forge;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModPlatformImpl {
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {
        ModLoadingContext.get().registerConfig(type, spec);
    }
    public static boolean canPlayerSleepNow(final Player player) {
        // TODO: Bed Groups
        // Only works on server side! If called on client, will always return false. All the more reason to use Bed Groups.
        return ForgeEventFactory.fireSleepingTimeCheck(player, player.getSleepingPos());
    }
}

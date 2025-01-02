package com.cosmicdan.sleepingoverhaul.fabric;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Optional;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModPlatformImpl {
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {
        ForgeConfigRegistry.INSTANCE.register(SleepingOverhaul.MOD_ID, type, spec);
    }

    public static boolean canPlayerSleepNow(final Player player) {
        // TODO: Bed Groups
        boolean isDay = player.level().isDay();
        final Optional<BlockPos> bedPosMaybe = player.getSleepingPos();
        if (bedPosMaybe.isPresent()) {
            final BlockPos bedPos = bedPosMaybe.get();
            // Probably only work on server-side (same as Forge, vanilla MC thing). All the more reason for Bed Groups.
            InteractionResult result = EntitySleepEvents.ALLOW_SLEEP_TIME.invoker().allowSleepTime(player, bedPos, !isDay);
            if (result != InteractionResult.PASS) {
                return result.consumesAction();
            }
        }
        return !isDay;
    }
}

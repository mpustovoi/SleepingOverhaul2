package github.cosmicdan.sleepingoverhaul;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModPlatform {
    @ExpectPlatform
    public static void registerConfig(final ModConfig.Type type, final IConfigSpec<ForgeConfigSpec> spec) {}

    @ExpectPlatform
    public static boolean canPlayerSleepNow(final Player player) {
        return false;
    }
}

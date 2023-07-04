package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.cosmiclib.mixinex.ModifyConstantChecked;
import com.cosmicdan.sleepingoverhaul.mixin.errorhandler.MinecraftServerMixinFailure.GetMsPerTickFailure;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    private static final long MS_PER_TICK_NORMAL = 50L;

    //@ModifyConstantEx(method = "runServer", onFailure = GetMsPerTickFailure.class, constant = @Constant(longValue = 50L), require = 4, allow = 4)
    @ModifyConstantChecked(method = "runServer", onFailure = GetMsPerTickFailure.class, constant = @Constant(longValue = 50L), require = 3, allow = 3)
    private static long getMsPerTick(final long currentMsPerTick) {
        if (currentMsPerTick != MS_PER_TICK_NORMAL) {
            // TODO: something else has modified the server MS per tick...?
            //       Log warning (only once, to prevent spam)
        }
        //return 5L;
        //return MS_PER_TICK_NORMAL;
        return 1L;
    }
}

package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.cosmiclib.mixinex.ModifyConstantChecked;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.errorhandler.MinecraftServerMixinFailure.GetMsPerTickFailure;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Responsible for changing the tick rate during timelapse
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract ServerLevel overworld();

    //private static final long MS_PER_TICK_NORMAL = 50L;

    /*
    @ModifyConstantChecked(
            method = "runServer",
            onFailure = GetMsPerTickFailure.class,
            constant = @Constant(longValue = 50L), require = 4, allow = 4
    )

     */
    @ModifyConstant(
            method = "runServer()V",
            constant = @Constant(longValue = 50L), require = 4, allow = 4
    )
    private long getMsPerTick(final long currentMsPerTick) {
        /*
        if (currentMsPerTick != MS_PER_TICK_NORMAL) {
            // TODO: something else has modified the server MS per tick...?
            //       Log warning (only once, to prevent spam)
        }
         */
        if (SleepingOverhaul.timelapseEnd > 0)
            return 1L;
        else
            return currentMsPerTick;
    }
}

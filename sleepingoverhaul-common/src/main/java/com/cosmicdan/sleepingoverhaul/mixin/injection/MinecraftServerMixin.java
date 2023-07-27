package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BooleanSupplier;

/**
 * Responsible for changing the tick rate during timelapse
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    private final BooleanSupplier alwaysTrueSupplier = MinecraftServerMixin::isTrue;

    @Shadow public abstract void tickServer(BooleanSupplier booleanSupplier);

    @Shadow protected abstract boolean haveTime();

    @Redirect(
            method = "runServer()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.tickServer (Ljava/util/function/BooleanSupplier;)V")
    )
    private void onCallTickServer(MinecraftServer self, BooleanSupplier haveTimeSupplier) {
        if (SleepingOverhaul.serverState.timelapsePending()) {
            while(haveTime()) {
                tickServer(alwaysTrueSupplier);
            }
        } else {
            tickServer(haveTimeSupplier);
        }
    }

    private static boolean isTrue() {
        return true;
    }
}

package com.cosmicdan.sleepingoverhaul.mixin.injection_old;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

/**
 * Hook responsible for changing the tick rate during timelapse
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    private final BooleanSupplier alwaysTrueSupplier = MinecraftServerMixin::isTrue;

    @Shadow public abstract void tickServer(BooleanSupplier booleanSupplier);

    @Shadow protected abstract boolean haveTime();

    @WrapOperation(
            method = "runServer()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.tickServer (Ljava/util/function/BooleanSupplier;)V"),
            require = 1, allow = 1
    )
    public final void onCallTickServer(MinecraftServer self, BooleanSupplier haveTimeSupplier, Operation<Void> original) {
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

package com.cosmicdan.sleepingoverhaul.fabric.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows to lay in bed at any time when bedrest is enabled. We re-implement the sleep time check elsewhere.
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerDaycheckHook {
    @WrapOperation(
            method = "startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/Level.isDay ()Z"),
            require = 1, allow = 1
    )
    public boolean onIsDay(final Level self, final Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return false;
        else
            return original.call(self);
    }
}

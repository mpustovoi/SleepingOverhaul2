package com.cosmicdan.sleepingoverhaul.forge.mixin.injection_old;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public class ServerPlayerForgeMixin {
    /**
     * If bed rest is enabled, always return true for the fireSleepingTimeCheck call (Forge event) in startSleepInBed.
     * Also we prevent that event firing, and do it ourselves later (when the player actually sleeps).
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"),
            require = 1, allow = 1,
            remap = false
    )
    private boolean onStartSleepAtFireTimeCheck(Player player, Optional<BlockPos> sleepingLocation, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return true;
        else
            return original.call(player, sleepingLocation);
    }
}

package com.cosmicdan.sleepingoverhaul.forge.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

public class BedRestMixinsForge {}

@Mixin(ServerPlayer.class)
abstract class BedRestMixinsForgeServerPlayer {

    /**
     * For Bed Rest, remove isDay check during tick. We perform the check later in ServerState#onReallySleepingRecv
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"),
            remap = false
    )
    private boolean onTimeCheck(Player player, Optional<BlockPos> sleepingLocation, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return true;
        else
            return original.call(player, sleepingLocation);
    }
}

@Mixin(Player.class)
abstract class BedRestMixinsForgePlayer {

    /**
     * For Bed Rest, remove isDay check during tick. We perform the check later in ServerState#onReallySleepingRecv
     */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"),
            remap = false
    )
    private boolean onTimeCheck(Player player, Optional<BlockPos> sleepingLocation, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return true;
        else
            return original.call(player, sleepingLocation);
    }
}

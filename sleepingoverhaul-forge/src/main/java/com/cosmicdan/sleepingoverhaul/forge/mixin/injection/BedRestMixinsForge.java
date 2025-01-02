package com.cosmicdan.sleepingoverhaul.forge.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

public class BedRestMixinsForge {}

@Mixin(ServerPlayer.class)
abstract class BedRestMixinsForgeServerPlayer {
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"),
            remap = false
    )
    private boolean onTimeCheck(Player player, Optional<BlockPos> sleepingLocation, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return true; // We do not re-fire this event on the client, since it's only used to kick the player out of bed
        else
            return original.call(player, sleepingLocation);
    }
}

@Mixin(Player.class)
abstract class BedRestMixinsForgePlayer {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"),
            remap = false
    )
    private boolean onTimeCheck(Player player, Optional<BlockPos> sleepingLocation, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return true; // We check for correct sleeping time later in ServerState#onReallySleepingRecv
        else
            return original.call(player, sleepingLocation);
    }
}

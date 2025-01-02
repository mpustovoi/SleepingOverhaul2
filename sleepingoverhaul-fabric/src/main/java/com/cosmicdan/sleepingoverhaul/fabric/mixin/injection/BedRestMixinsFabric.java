package com.cosmicdan.sleepingoverhaul.fabric.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class BedRestMixinsFabric {}

@Mixin(ServerPlayer.class)
abstract class BedRestMixinsFabricServerPlayer {
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z")
    )
    private boolean onIsDayCheck(Level instance, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return false; // We do not re-fire this event on the client, since it's only used to kick the player out of bed
        else
            return original.call(instance);
    }
}

@Mixin(Player.class)
abstract class BedRestMixinsFabricPlayer {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z")
    )
    private boolean onIsDayCheck(Level instance, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return false; // We check for correct sleeping time later in ServerState#onReallySleepingRecv
        else
            return original.call(instance);
    }
}

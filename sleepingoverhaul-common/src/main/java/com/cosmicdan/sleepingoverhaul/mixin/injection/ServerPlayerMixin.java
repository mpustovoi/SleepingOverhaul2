package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifications to enable configurable sleeping rules/checks
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    /*
    @Inject(at = @At("HEAD"), method = "startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;")
    private void startSleepInBed(final BlockPos bed, final CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> callback) {
        // DO NOTHING, kept as reference - need to replace individual things within the target.
    }

     */

    // TODO: Implement this daytime sleep check for Fabric (Forge injects it's own event)
    //public startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;
    //INVOKEVIRTUAL net/minecraft/world/level/Level.isDay ()Z
    /*
    @Redirect(
            method = "startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/Level.isDay ()Z")
    )
    private boolean onIsDaySleepCheck(final Level instance) {
        if (SleepingOverhaul.CONFIG_SERVER.bedRestEnabled.get())
            return false; // return false to allow bed rest
        else
            return instance.isDay();
    }
     */

}

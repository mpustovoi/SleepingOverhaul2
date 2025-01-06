package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class BedRestMixinsCommon {}

@Mixin(Player.class)
abstract class BedRestMixinsCommonPlayer implements PlayerMixinProxy {
    /**
     * Flag used to confirm that the player has actually pressed Sleep and is not just Bed Resting
     */
    private boolean reallySleeping = false;

    @Shadow
    private int sleepCounter;

    /**
     * Cap the screen dim value if Bed Resting. Not only for visual effect (client), but to prevent deepSleep status
     */
    @Inject(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;sleepCounter:I", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER)
    )
    private void afterSleepCounterIncrement(CallbackInfo ci) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
            if (!isReallySleeping()) {
                final int dimScreenValue = SleepingOverhaul.serverConfig.bedRestScreenDimValue.get();
                if (sleepCounter > dimScreenValue) {
                    sleepCounter = dimScreenValue;
                }
            }
        }
    }

    /**
     * On stop sleep, also update reallySleeping flag. Called on both server and client so no need for a packet here.
     */
    @Inject(
            method = "stopSleepInBed",
            at = @At("HEAD")
    )
    private void onStopSleepInBed(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        setReallySleeping(false);
    }

    @Override
    public final void setReallySleeping(final boolean isReallySleeping) {
        reallySleeping = isReallySleeping;
    }

    @Override
    public final boolean isReallySleeping() {
        return reallySleeping;
    }
}

@Mixin(SleepStatus.class)
abstract class BedRestMixinsCommonSleepStatus {

    /**
     * For MP, check reallySleeping instead so announcements don't mistakenly fire on Bed Rest
     */
    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z")
    )
    private boolean onUpdateIsSleepingCheck(ServerPlayer serverPlayer, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
            return ((PlayerMixinProxy) serverPlayer).isReallySleeping();
        } else {
            return original.call(serverPlayer);
        }
    }
}

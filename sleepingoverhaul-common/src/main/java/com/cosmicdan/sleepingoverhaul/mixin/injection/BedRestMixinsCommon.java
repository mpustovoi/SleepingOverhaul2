package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class BedRestMixinsCommon {}

@Mixin(Player.class)
abstract class BedRestMixinsCommonPlayer implements PlayerMixinProxy {
    private boolean reallySleeping = false;

    @Shadow
    private int sleepCounter;

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

    @Inject(
            method = "stopSleepInBed",
            at = @At("RETURN")
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

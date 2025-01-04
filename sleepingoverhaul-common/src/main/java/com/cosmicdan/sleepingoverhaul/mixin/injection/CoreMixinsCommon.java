package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

public class CoreMixinsCommon {}

@Mixin(ServerLevel.class)
abstract class CoreMixinsCommonServerLevel extends Level {
    protected CoreMixinsCommonServerLevel(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow protected abstract void wakeUpAllPlayers();

    @Shadow protected abstract void resetWeatherCycle();

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V")
    )
    private void onSetDayTime(ServerLevel instance, long targetTime, Operation<Void> original) {
        // We perform the sleep action here so we can capture the time, since other mods may have modified it (e.g. Hammocks)
        switch (SleepingOverhaul.serverConfig.sleepAction.get()) {
            case Timelapse -> {
                final boolean hasTimelapseStopped = !SleepingOverhaul.serverState.didTickTimelapse(instance, getDayTime(), targetTime);
                if (hasTimelapseStopped) {
                    wakeUpAllPlayers();
                    so2_$resetWeatherCycleIfNeeded();
                }
            }
            case SkipTime -> {
                original.call(instance, targetTime);
                wakeUpAllPlayers();
                so2_$resetWeatherCycleIfNeeded();
            }
            case Nothing -> {
                // do nothing
            }
        }
    }

    @WrapOperation(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", ordinal = 1, target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z"),
            require = 1, allow = 1
    )
    public final boolean shouldGetBooleanGameRuleSecond(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original) {
        if (key.equals(GameRules.RULE_WEATHER_CYCLE)) { // should always be true, we verify it anyway
            return false; // Always false here, we reset weather ourselves at the appropriate time
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the second GameRules.getBoolean call was not RULE_WEATHER_CYCLE! Mod conflict...?");
        }
    }

    @Unique
    public void so2_$resetWeatherCycleIfNeeded() {
        if (getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && isRaining() && SleepingOverhaul.serverConfig.resetWeatherOnWake.get())
            resetWeatherCycle();
    }
}

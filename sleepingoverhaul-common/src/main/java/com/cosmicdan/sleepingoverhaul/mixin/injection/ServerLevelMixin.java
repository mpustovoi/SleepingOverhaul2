package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.server.ServerState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Various hooks, refer to each each method
 *
 * @author Daniel 'CosmicDan' Connolly
 */
@SuppressWarnings("MethodMayBeStatic")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements WorldGenLevel {
    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow protected abstract void wakeUpAllPlayers();

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Hooks and replaces GameRules.RULE_DAYLIGHT check to handle the lifecycle and such of custom sleep actions
     */
    @WrapOperation(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z"),
            require = 1, allow = 1
    )
    public final boolean shouldGetBooleanGameRuleFirst(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original) {
        if (key.equals(GameRules.RULE_DAYLIGHT)) { // should always be true
            if (gameRules.getBoolean(key)) {
                switch (SleepingOverhaul.serverConfig.sleepAction.get()) {
                    case Timelapse -> {
                        final boolean timelapseStopped = !SleepingOverhaul.serverState.tickTimelapse(getLevel());
                        if (timelapseStopped) {
                            wakeUpAllPlayers();
                            resetWeatherCycleIfNeeded();
                        }
                    }
                    case SkipToDay -> {
                        setDayTime(ServerState.getNextMorning(getLevel()));
                        wakeUpAllPlayers();
                        resetWeatherCycleIfNeeded();
                    }
                    case Nothing -> {
                        // do nothing
                    }
                }
            }
            return false; // never do vanilla behaviour, we reproduce it above
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the first GameRules.getBoolean call was not RULE_DAYLIGHT! Mod conflict...?");
        }
    }

    public final void resetWeatherCycleIfNeeded() {
        if (getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && isRaining() && SleepingOverhaul.serverConfig.morningResetWeather.get())
            resetWeatherCycle();
    }

    /**
     * Hooks and forces-false on GameRules.RULE_WEATHER_CYCLE check; since we need to do it ourselves at the appropriate time
     */
    @WrapOperation(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", ordinal = 1, target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z"),
            require = 1, allow = 1
    )
    public final boolean shouldGetBooleanGameRuleSecond(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original) {
        if (key.equals(GameRules.RULE_WEATHER_CYCLE)) { // should always be true, we verify it anyway
            return false; // always return false; we handle it in shouldGetBooleanGameRuleFirst
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the second GameRules.getBoolean call was not RULE_WEATHER_CYCLE! Mod conflict...?");
        }
    }

    /**
     * Hooks and cancels wakeUpAllPlayers; we need to do it ourselves at the appropriate time
     */
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V"),
            require = 1, allow = 1
    )
    public final void onWakeUpAllPlayers(ServerLevel self) {
        // Do nothing always, we wake players ourselves at the appropriate time
        return;
    }

    /**
     * Hooks the sky brightness check since it is a convenient place to check if timelapse has reached end time.
     */
    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSkyBrightness()V"),
            require = 1, allow = 1
    )
    public final void onUpdateSkyBrightness(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        SleepingOverhaul.serverState.onBeforeTickTime(getLevel());
    }

    @Shadow
    private void resetWeatherCycle() { throw new AssertionError(); }

    @Shadow
    public void setDayTime(final long dayTime) { throw new AssertionError(); }

    @Shadow @Final
    EntityTickList entityTickList;

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow public abstract ServerLevel getLevel();

    /**
     * Enables feature for preventing spawns during timelapse
     */
    @Inject(
            method = "isNaturalSpawningAllowed(Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1, allow = 1
    )
    public final void onNaturalSpawnCheckBlockPos(final BlockPos blockPos, final CallbackInfoReturnable<Boolean> cir) {
        if (SleepingOverhaul.serverConfig.disableNaturalSpawning.get() && (SleepingOverhaul.serverState.timelapsePending()))
            cir.setReturnValue(false);
    }

    /**
     * Enables feature for preventing spawns during timelapse
     */
    @Inject(
            method = "isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1, allow = 1
    )
    public final void onNaturalSpawnCheckChunk(ChunkPos chunkPoss, final CallbackInfoReturnable<Boolean> cir) {
        if (SleepingOverhaul.serverConfig.disableNaturalSpawning.get() && (SleepingOverhaul.serverState.timelapsePending()))
            cir.setReturnValue(false);
    }

    /*
    // GETFIELD net/minecraft/server/level/ServerLevel.entityTickList : Lnet/minecraft/world/level/entity/EntityTickList;
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;entityTickList:Lnet/minecraft/world/level/entity/EntityTickList;")
    )
    private EntityTickList onGetEntityTickList(final ServerLevel self) {
        // TODO: return an empty list if timelapse active and "skip entity ticks" is configured
        return entityTickList;
    }
    */
}

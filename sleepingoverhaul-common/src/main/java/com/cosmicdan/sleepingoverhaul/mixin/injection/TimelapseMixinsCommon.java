package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TimelapseMixinsCommon {}

@Mixin(MinecraftServer.class)
abstract class TimelapseMixinsCommonMinecraftServer {
    @Shadow public abstract void tickServer(BooleanSupplier booleanSupplier);

    @Shadow protected abstract boolean haveTime();

    @WrapOperation(
            method = "runServer()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.tickServer (Ljava/util/function/BooleanSupplier;)V"),
            require = 1, allow = 1
    )
    public final void onCallTickServer(MinecraftServer self, BooleanSupplier haveTimeSupplier, Operation<Void> original) {
        if (SleepingOverhaul.serverState.isTimelapseActive()) {
            // tick server as many times as possible
            while(haveTime()) {
                tickServer(this::alwaysTrue);
            }
        } else {
            original.call(self, haveTimeSupplier);
        }
    }

    private boolean alwaysTrue() {
        return true;
    }
}

@Mixin(ServerLevel.class)
abstract class TimelapseMixinsCommonServerLevel extends Level {
    protected TimelapseMixinsCommonServerLevel(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow protected abstract void wakeUpAllPlayers();

    @Shadow protected abstract void resetWeatherCycle();

    @Shadow public abstract MinecraftServer getServer();

    @Shadow
    public abstract ServerLevel getLevel();

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
            case SkipToDay -> {
                original.call(instance, targetTime);
                wakeUpAllPlayers();
                so2_$resetWeatherCycleIfNeeded();
            }
            case Nothing -> {
                // do nothing
            }
        }
    }

    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V"),
            require = 1, allow = 1
    )
    public final void onWakeUpAllPlayers(ServerLevel self) {
        // Do nothing (never wake players here), we do it ourselves at the appropriate time
    }

    /**
     * For feature to prevent spawns during timelapse
     */
    @Inject(
            method = "isNaturalSpawningAllowed(Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void onNaturalSpawnCheckBlockPos(final BlockPos blockPos, final CallbackInfoReturnable<Boolean> cir) {
        if (SleepingOverhaul.serverState.shouldPreventNaturalSpawning())
            cir.setReturnValue(false);
    }

    /**
     * For feature to prevent spawns during timelapse
     */
    @Inject(
            method = "isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void onNaturalSpawnCheckChunk(ChunkPos chunkPoss, final CallbackInfoReturnable<Boolean> cir) {
        if (SleepingOverhaul.serverState.shouldPreventNaturalSpawning())
            cir.setReturnValue(false);
    }

    // TODO: Move to "GeneralMixinsCommonServerLevel"
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

    // TODO: Move to "GeneralMixinsCommonServerLevel" (as above)
    @Unique
    public void so2_$resetWeatherCycleIfNeeded() {
        if (getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && isRaining() && SleepingOverhaul.serverConfig.resetWeatherOnWake.get())
            resetWeatherCycle();
    }

    @WrapOperation(
            method = "announceSleepStatus",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z")
    )
    public boolean onAreEnoughSleeping(SleepStatus instance, int requiredSleepPercentage, Operation<Boolean> original) {
        // MULTIPLAYER ONLY: Ensure timelapse is disabled if not enough players are sleeping
        final boolean areEnoughSleeping = original.call(instance, requiredSleepPercentage);
        if (!areEnoughSleeping && SleepingOverhaul.serverConfig.sleepAction.get() == ServerConfig.SleepAction.Timelapse) {
            SleepingOverhaul.serverState.stopTimelapseNow(getLevel());
        }
        return areEnoughSleeping;
    }
}

@Mixin(Player.class)
abstract class TimelapseMixinsCommonPlayer extends LivingEntity {
    protected TimelapseMixinsCommonPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "stopSleepInBed",
        at = @At(value = "RETURN")
    )
    public void onStopSleepInBed(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        MinecraftServer server = getServer();
        if (server != null) {
            if (server.isSingleplayer() && level() instanceof ServerLevel serverLevel) {
                // SINGLE-PLAYER ONLY: Immediately stop timelapse
                SleepingOverhaul.serverState.stopTimelapseNow(serverLevel);
            }
        }
        // CLIENT-ONLY - Stop cinematic
        SleepingOverhaul.clientState.setTimelapseCamera((Player) (Object) this, true);
    }
}

@Mixin(LivingEntity.class)
abstract class TimelapseMixinsCommonLivingEntity {
    /**
     * For feature to prevent LivingEntity from travel during timelapse
     */
    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void onTravel(Vec3 vec3, CallbackInfo ci) {
        if (SleepingOverhaul.serverState.shouldPreventLivingTravel())
            ci.cancel();
    }
}

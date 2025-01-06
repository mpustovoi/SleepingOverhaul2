package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TimelapseMixinsCommon {}

@Mixin(MinecraftServer.class)
abstract class TimelapseMixinsCommonMinecraftServer {
    @Shadow public abstract void tickServer(BooleanSupplier booleanSupplier);

    @Shadow protected abstract boolean haveTime();

    /**
     * Under Timelapse, tick server as many times as possible.
     */
    @WrapOperation(
            method = "runServer()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.tickServer (Ljava/util/function/BooleanSupplier;)V"),
            require = 1, allow = 1
    )
    public final void onCallTickServer(MinecraftServer self, BooleanSupplier haveTimeSupplier, Operation<Void> original) {
        if (SleepingOverhaul.serverState.isTimelapseActive()) {
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

    @Shadow public abstract MinecraftServer getServer();

    @Shadow
    public abstract ServerLevel getLevel();

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

    /**
     * For MP, ensure timelapse is disabled if not enough players are sleeping
     * Regarding Bed Rest, we still keep the check for regular sleep since BedRestMixinsCommonSleepStatus changes the
     * counter to check for reallySleeping (also Multiplayer-only).
     */
    @WrapOperation(
            method = "announceSleepStatus",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z")
    )
    public boolean onAreEnoughSleeping(SleepStatus instance, int requiredSleepPercentage, Operation<Boolean> original) {
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

    /**
     * For SP, immediately stop Timelapse and clear the camera
     */
    @Inject(
        method = "stopSleepInBed",
        at = @At(value = "RETURN")
    )
    public void onStopSleepInBed(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        if (level() instanceof ServerLevel serverLevel) {
            if (getServer().isSingleplayer()) {
                SleepingOverhaul.serverState.stopTimelapseNow(serverLevel);
            }
        }
        // Client-only but no need for check since Server just has ClientStateDummy
        SleepingOverhaul.clientState.setTimelapseCamera((Player) (Object) this, false);
    }
}

@Mixin(LivingEntity.class)
abstract class TimelapseMixinsCommonLivingEntity extends Entity {
    public TimelapseMixinsCommonLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * For feature to prevent LivingEntity travel during timelapse
     */
    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTravel(Vec3 vec3, CallbackInfo ci) {
        //noinspection ConstantValue
        if (SleepingOverhaul.serverState.shouldPreventLivingTravel() && !((Object) this instanceof Player))
            ci.cancel();
    }

    @WrapMethod(
            method = "isImmobile"
    )
    private boolean onIsImmobileCheck(Operation<Boolean> original) {
        if (SleepingOverhaul.serverState.isTimelapseActive() && SleepingOverhaul.serverConfig.noMovementDuringTimelapse.get()) {
            if ((Object) this instanceof Player) {
                return true;
            }
        }
        return original.call();
    }
}

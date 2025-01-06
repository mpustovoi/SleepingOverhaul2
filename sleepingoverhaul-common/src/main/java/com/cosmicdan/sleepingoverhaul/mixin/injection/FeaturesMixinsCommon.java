package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

public class FeaturesMixinsCommon {}

@Mixin(BedBlock.class)
abstract class FeaturesMixinsCommonBedBlock {
    /**
     * For feature to allow rest/sleep in any dimension
     */
    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z")
    )
    private boolean onUseCanSetSpawn(Level level, Operation<Boolean> original) {
        boolean canSetSpawn = original.call(level);
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureAllowAnyDimension.get())
            canSetSpawn = true;
        return canSetSpawn;
    }
}

@Mixin(Player.class)
abstract class FeaturesMixinsCommonPlayer {
    /**
     * For feature to allow setting spawn in any dimension
     */
    @WrapOperation(
            method = "findRespawnPositionAndUseSpawnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z")
    )
    private static boolean onFindRespawnCanSetSpawn(Level level, Operation<Boolean> original) {
        boolean canSetSpawn = original.call(level);
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureSetSpawnAnyDimension.get())
            canSetSpawn =  true;
        return canSetSpawn;
    }
}


@Mixin(ServerPlayer.class)
abstract class FeaturesMixinsCommonServerPlayer {
    @Shadow
    public abstract ServerLevel serverLevel();

    /**
     * For feature to allow rest/sleep in any dimension
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;natural()Z")
    )
    private boolean onStartSleepInBedNaturalDimensionCheck(DimensionType instance, Operation<Boolean> original) {
        boolean isNatural = instance.natural();
        if (!isNatural && SleepingOverhaul.serverConfig.featureAllowAnyDimension.get())
            isNatural = true;
        return isNatural;
    }

    /**
     * For feature to allow setting spawn in any dimension
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V")
    )
    private void onStartSleepInBedSetRespawn(ServerPlayer instance, ResourceKey<Level> levelResourceKey, BlockPos dimension, float position, boolean angle, boolean forced, Operation<Void> original) {
        boolean canSetSpawn = serverLevel().dimensionType().bedWorks();
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureSetSpawnAnyDimension.get())
            canSetSpawn = true;
        if (canSetSpawn)
            original.call(instance, levelResourceKey, dimension, position, angle, forced);
    }
}

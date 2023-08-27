package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@SuppressWarnings("MethodWithTooManyParameters")
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    // spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V
    @Inject(
            method = "spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onSpawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnState spawnState, boolean bl, boolean bl2, boolean bl3, CallbackInfo ci) {
        if (SleepingOverhaul.serverConfig.disableNaturalSpawning.get() && (SleepingOverhaul.serverState.timelapsePending()))
            ci.cancel();
    }
}

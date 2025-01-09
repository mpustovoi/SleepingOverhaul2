package com.cosmicdan.sleepingoverhaul.server;

import com.cosmicdan.sleepingoverhaul.ModPlatform;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ServerState {
    // -1 = inactive
    private long timelapseEnd = -1;

    // these are used for logging only
    private long timelapseStartNanos = 0;
    private long timelapseTickCount = 0;

    public ServerState() {
        NetworkManager.registerReceiver(Side.C2S, SleepingOverhaul.PACKET_TRY_REALLY_SLEEPING, this::tryReallySleepingRecv);
    }

    public boolean isTimelapseActive() {
        return timelapseEnd > -1;
    }

    public boolean didTickTimelapse(ServerLevel serverLevel, long currentTime, long targetTime) {
        if (timelapseEnd == -1) {
            // start timelapse
            // we need to remember the initial targetTime, otherwise timelapse could continue forever
            timelapseEnd = targetTime;
            notifyPlayersTimelapseChange(serverLevel.players(), timelapseEnd);
            onTimelapseStart();
        } else if (currentTime >= timelapseEnd) {
            // stop timelapse
            stopTimelapseNow(serverLevel);
        }
        return timelapseEnd > -1;
    }

    public void onServerTickPost(MinecraftServer server) {
        if ((timelapseEnd > -1) && SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get())
            timelapseTickCount++;
        // DEV TEST ONLY: Spawn a Zombie on top of player at first midnight
        if (false) {
            if (server.overworld().getDayTime() == 18000) {
                final Optional<ServerPlayer> firstPlayerMaybe = server.getPlayerList().getPlayers().stream().findFirst();
                if (firstPlayerMaybe.isPresent()) {
                    final BlockPos firstPlayerPosAbove = firstPlayerMaybe.get().getOnPos().above(2);
                    EntityType.ZOMBIE.spawn(server.overworld(), firstPlayerPosAbove, MobSpawnType.SPAWNER);
                }
            }
        }
        // DEV TEST ONLY: Apply poison to player at first midnight
        if (false) {
            if (server.overworld().getDayTime() == 18000) {
                final Optional<ServerPlayer> firstPlayerMaybe = server.getPlayerList().getPlayers().stream().findFirst();
                if (firstPlayerMaybe.isPresent()) {
                    firstPlayerMaybe.get().addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.POISON, 1000, 3)));
                }
            }
        }
    }

    public void onTimelapseStart() {
        if (SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get()) {
            timelapseStartNanos = Util.getNanos();
            timelapseTickCount = 0;
        }
    }

    public void onTimelapseEnd() {
        if (SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get()) {
            final double timelapseSeconds = (Util.getNanos() - timelapseStartNanos) / 1000000.0 / 1000.0;
            final double ticksPerSecond = timelapseTickCount / timelapseSeconds;
            SleepingOverhaul.LOGGER.info("Timelapse finished. Average TPS = {}; total time = {} seconds; total ticks = {}", ticksPerSecond, timelapseSeconds, timelapseTickCount);
        }
    }

    private void tryReallySleepingRecv(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        final Player player = context.getPlayer();
        boolean reallySleeping = buf.readBoolean();
        if (reallySleeping && ModPlatform.canPlayerSleepNow(player)) {
            //noinspection CastToIncompatibleInterface
            ((PlayerMixinProxy) player).so2_$setReallySleeping(reallySleeping);
        } else {
            reallySleeping = false;
        }
        if (player instanceof ServerPlayer serverPlayer) { // should always be true
            if (!reallySleeping) {
                final FriendlyByteBuf bufPong = new FriendlyByteBuf(Unpooled.buffer());
                bufPong.writeBoolean(false);
                NetworkManager.sendToPlayer(serverPlayer, SleepingOverhaul.PACKET_TRY_REALLY_SLEEPING, bufPong);
            } else {
                if (SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
                    // Update sleeping list because we've made it check for reallySleeping in BedRestMixinsCommonSleepStatus
                    serverPlayer.serverLevel().updateSleepingPlayerList();
                }
            }
        } else {
            SleepingOverhaul.LOGGER.warn("The player instance received from packet is not ServerPlayer, eh? Forge/Fabric changed stuff? Bed rest will be bugged...!");
        }
    }

    private void notifyPlayersTimelapseChange(final Iterable<ServerPlayer> players, final long timelapseEnd) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(timelapseEnd);
        NetworkManager.sendToPlayers(players, SleepingOverhaul.PACKET_TIMELAPSE_CHANGE, buf);
    }

    public void stopTimelapseNow(ServerLevel serverLevel) {
        if (isTimelapseActive()) {
            timelapseEnd = -1;
            notifyPlayersTimelapseChange(serverLevel.players(), timelapseEnd);
            onTimelapseEnd();
        }
    }

    public float getPlayerHurtAdj(ServerPlayer player, DamageSource source, float amount) {
        float amountAdjusted = amount;
        if (isTimelapseActive()) {
            if (!source.isIndirect() && player.isSleeping()) {
                // timelapse active and player was attacked by direct damage
                switch (SleepingOverhaul.serverConfig.timelapseSleepersDirectDamageAction.get()) {
                    case NoChange -> {}
                    case InstantKill -> amountAdjusted = Float.POSITIVE_INFINITY;
                    case Invincible -> amountAdjusted = Float.NaN;
                }
            } else if (!player.isSleeping() && SleepingOverhaul.serverConfig.noDamageToNonSleepers.get()) {
                amountAdjusted = Float.NaN;
            }
        }
        return amountAdjusted;
    }

    public boolean shouldPreventLivingTravel() {
        return (isTimelapseActive() && SleepingOverhaul.serverConfig.disableLivingEntityTravel.get());
    }

    public boolean shouldPreventNaturalSpawning() {
        return (isTimelapseActive() && SleepingOverhaul.serverConfig.disableNaturalSpawning.get());
    }

    /**
     * Called on the client to sync the value from server
     */
    public void setTimelapseEndForClient(long timelapseEndIn) {
        timelapseEnd = timelapseEndIn;
    }
}

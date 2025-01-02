package com.cosmicdan.sleepingoverhaul.server;

import com.cosmicdan.sleepingoverhaul.ModPlatform;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.google.common.graph.Network;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public void onServerTickPost() {
        if ((timelapseEnd > 0) && SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get())
            timelapseTickCount++;
    }

    public boolean tickTimelapse(ServerLevel level) {
        boolean timelapseActive = true;
        if (timelapseEnd == -1) {
            // timelapse has just started
            notifyPlayersTimelapseChange(level.players(), true);
            timelapseEnd = getNextMorning(level);
            onTimelapseStart();
        } else if (timelapseEnd == -2) {
            // timelapse has reached its target tick
            timelapseActive = false;
            notifyPlayersTimelapseChange(level.players(), false);
            timelapseEnd = -1;
            onTimelapseEnd();
        }

        return timelapseActive;
    }

    public static long getNextMorning(ServerLevel level) {
        final long oneDayAhead = level.getLevelData().getDayTime() + Level.TICKS_PER_DAY;
        final long nextMorning = oneDayAhead - (oneDayAhead % Level.TICKS_PER_DAY); // next multiple of 24000 = 06:00
        // calculate the tick offset according to config
        /*
        final int nextMorningHourOffset = (-6 + SleepingOverhaul.CONFIG_SERVER.morningHour.get()) * 1000;
        final int nextMorningMinuteOffset = Math.round(SleepingOverhaul.CONFIG_SERVER.morningMinute.get() * (1000 / 60.0f));
        final int nextMorningOffset = nextMorningHourOffset + nextMorningMinuteOffset;
        return nextMorning + nextMorningOffset;
         */
        return nextMorning;
    }

    private void notifyPlayersTimelapseChange(final Iterable<ServerPlayer> players, final boolean timelapseActive) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(timelapseActive);
        NetworkManager.sendToPlayers(players, SleepingOverhaul.PACKET_TIMELAPSE_CHANGE, buf);
    }

    private void tryReallySleepingRecv(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        final Player player = context.getPlayer();
        boolean reallySleeping = buf.readBoolean();
        if (reallySleeping && ModPlatform.canPlayerSleepNow(player)) {
            //noinspection CastToIncompatibleInterface
            ((PlayerMixinProxy) player).setReallySleeping(reallySleeping);
        } else {
            reallySleeping = false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (!reallySleeping) {
                final FriendlyByteBuf bufPong = new FriendlyByteBuf(Unpooled.buffer());
                bufPong.writeBoolean(false);
                NetworkManager.sendToPlayer(serverPlayer, SleepingOverhaul.PACKET_TRY_REALLY_SLEEPING, bufPong);
            }
        } else {
            SleepingOverhaul.LOGGER.warn("The player instance received from packet is not ServerPlayer, eh? Forge/Fabric changed stuf? Client screen-dim will be bugged...");
        }
    }

    public void onBeforeTickTime(ServerLevel level) {
        //System.out.println(getDayTime() + ">=" + SleepingOverhaul.timelapseEnd);
        if (timelapseEnd > 0) {
            // timelapse is currently active
            if (level.getDayTime() >= timelapseEnd) {
                timelapseEnd = -2;
            }
        }
    }

    public boolean shouldPreventLivingTravel() {
        return (SleepingOverhaul.serverConfig.disableLivingEntityTravel.get() && (timelapseEnd > 0));
    }

    public boolean timelapsePending() {
        return timelapseEnd > 0;
    }

    public float getPlayerHurtAdj(ServerPlayer player, DamageSource source, float amount) {
        float amountAdjusted = amount;
        if (timelapsePending()) {
            // timelapse active and player was attacked
            if (SleepingOverhaul.serverConfig.sleepPreventMagicDamage.get() && source.isIndirect())
                amountAdjusted = Float.NaN;
            else {
                switch (SleepingOverhaul.serverConfig.sleepAttackedAction.get()) {
                    case NoChange -> {}
                    case InstantKill -> amountAdjusted = Float.POSITIVE_INFINITY;
                    case Invincible -> amountAdjusted = Float.NaN;
                }
            }
        }
        return amountAdjusted;
    }
}

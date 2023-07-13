package com.cosmicdan.sleepingoverhaul;

import com.cosmicdan.sleepingoverhaul.client.ClientState;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.mojang.logging.LogUtils;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.function.BooleanSupplier;

public class SleepingOverhaul {
    public static final String MOD_ID = "sleepingoverhaul";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation PACKET_REALLY_SLEEPING = new ResourceLocation(MOD_ID, "is_really_sleeping");
    public static final ResourceLocation PACKET_SLEEPERROR_TIME = new ResourceLocation(MOD_ID, "sleep_error_time");
    public static final ResourceLocation PACKET_TIMELAPSE_CHANGE = new ResourceLocation(MOD_ID, "timelapse_change");

    public static IClientState CLIENT_STATE = null;

    private static ForgeConfigSpec CONFIG_SPEC_SERVER = null;
    public static ServerConfig CONFIG_SERVER = null;
    private static ForgeConfigSpec CONFIG_SPEC_COMMON = null;
    public static CommonConfig CONFIG_COMMON = null;

    public static final BooleanSupplier ALWAYS_TRUE_SUPPLIER = SleepingOverhaul::alwaysTrue;

    public static long timelapseEnd = -1;
    // these are used for logging only
    private static long timelapseStartNanos = 0;
    private static long timelapseTickCount = 0;

    public static void init() {
        // Register server/world config
        final Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        CONFIG_SPEC_SERVER = specPairServer.getRight();
        CONFIG_SERVER = specPairServer.getLeft();
        ModConfigHelper.registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC_SERVER);
        // Common config
        /*
        final Pair<CommonConfig, ForgeConfigSpec> specPairCommon = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        CONFIG_SPEC_COMMON = specPairCommon.getRight();
        CONFIG_COMMON = specPairCommon.getLeft();
        ModConfigHelper.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC_COMMON);
         */

        NetworkManager.registerReceiver(Side.C2S, PACKET_REALLY_SLEEPING, SleepingOverhaul::onReallySleepingRecv);
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(Side.S2C, PACKET_SLEEPERROR_TIME, SleepingOverhaul::onSleepErrorTimeRecv);
            NetworkManager.registerReceiver(Side.S2C, PACKET_TIMELAPSE_CHANGE, SleepingOverhaul::onTimelapseChange);
            CLIENT_STATE = new ClientState();
        } else
            CLIENT_STATE = new ClientStateDummy();
    }

    public static boolean canSleepNow(final Level level) {
        return !level.isDay();
    }

    // Received on server
    private static void onReallySleepingRecv(FriendlyByteBuf buf, PacketContext context) {
        final Player player = context.getPlayer();
        if (canSleepNow(player.level)) {
            final boolean reallySleeping = buf.readBoolean();
            //noinspection CastToIncompatibleInterface
            ((PlayerMixinProxy) player).setReallySleeping(reallySleeping);
        } else {
            final FriendlyByteBuf bufPong = new FriendlyByteBuf(Unpooled.buffer());
            bufPong.writeBoolean(true);
            NetworkManager.sendToPlayer((ServerPlayer) player, PACKET_SLEEPERROR_TIME, bufPong);
        }
    }

    // Received on client
    private static void onSleepErrorTimeRecv(FriendlyByteBuf buf, PacketContext context) {
        context.getPlayer().displayClientMessage(Player.BedSleepingProblem.NOT_POSSIBLE_NOW.getMessage(), true);
        CLIENT_STATE.doSleepButtonCooldown();
    }

    // Received on client
    private static void onTimelapseChange(FriendlyByteBuf buf, PacketContext context) {
        final Player player = context.getPlayer();
        final boolean timelapseEnabled = buf.readBoolean();
        CLIENT_STATE.setTimelapseEnabled(timelapseEnabled);
    }

    private static boolean alwaysTrue() {
        return true;
    }

    public static void onTimelapseStart() {
        if (CONFIG_SERVER.logTimelapsePerformanceStats.get()) {
            timelapseStartNanos = Util.getNanos();
            timelapseTickCount = 0;
        }
    }

    public static void onTimelapseEnd() {
        if (CONFIG_SERVER.logTimelapsePerformanceStats.get()) {
            final double timelapseSeconds = (Util.getNanos() - timelapseStartNanos) / 1000000.0 / 1000.0;
            final double ticksPerSecond = timelapseTickCount / timelapseSeconds;
            LOGGER.info("Timelapse finished. Average TPS = " + ticksPerSecond + "; total time = " + timelapseSeconds + " seconds; total ticks = " + timelapseTickCount);
        }
    }

    public static void onServerTickPost() {
        if ((timelapseEnd > 0) && CONFIG_SERVER.logTimelapsePerformanceStats.get())
            timelapseTickCount++;
    }
}

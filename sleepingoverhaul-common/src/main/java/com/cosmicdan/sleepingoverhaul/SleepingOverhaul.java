package com.cosmicdan.sleepingoverhaul;

import com.cosmicdan.sleepingoverhaul.client.ClientConfig;
import com.cosmicdan.sleepingoverhaul.client.ClientState;
import com.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.cosmicdan.sleepingoverhaul.server.ServerState;
import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@SuppressWarnings({"StaticNonFinalField", "PublicField"})
public class SleepingOverhaul {
    public static final String MOD_ID = "sleepingoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ServerState serverState = null;
    public static IClientState clientState = null;

    public static ServerConfig serverConfig = null;
    public static ClientConfig clientConfig = null;

    public static final ResourceLocation PACKET_TRY_REALLY_SLEEPING = new ResourceLocation(SleepingOverhaul.MOD_ID, "is_really_sleeping");
    public static final ResourceLocation PACKET_SLEEPERROR_TIME = new ResourceLocation(SleepingOverhaul.MOD_ID, "sleep_error_time");
    public static final ResourceLocation PACKET_TIMELAPSE_CHANGE = new ResourceLocation(SleepingOverhaul.MOD_ID, "timelapse_change");

    //public static void init() {
    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    public SleepingOverhaul() {
        // Register server/world config
        final Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        serverConfig = specPairServer.getLeft();
        ModPlatform.registerConfig(Type.SERVER, specPairServer.getRight());

        serverState = new ServerState();
        if (Platform.getEnvironment() == Env.CLIENT) {
            clientState = new ClientState();
            // also register client config
            final Pair<ClientConfig, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            clientConfig = specPairClient.getLeft();
            ModPlatform.registerConfig(Type.CLIENT, specPairClient.getRight());
        } else
            clientState = new ClientStateDummy();


        EntityEvent.LIVING_HURT.register(SleepingOverhaul::onLivingHurt);
    }

    private static EventResult onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        EventResult eventResult = EventResult.pass(); // default = pass it on
        if (serverState.timelapsePending()) {
            if (entity instanceof ServerPlayer player) {
                if (! source.getMsgId().equals(TimelapseKillDamageSource.MSG_ID)) {
                    final float adjustedDamage = serverState.getPlayerHurtAdj(player, source, amount);
                    if (Float.isNaN(adjustedDamage))
                        // NaN = damage was cancelled
                        eventResult = EventResult.interruptFalse();
                    else if (Float.isInfinite(adjustedDamage)) {
                        // infinite = insta-kill configured
                        eventResult = EventResult.interruptFalse();
                        player.hurt(new TimelapseKillDamageSource(), Float.MAX_VALUE);
                    }
                }
            }
        }
        return eventResult;
    }
}

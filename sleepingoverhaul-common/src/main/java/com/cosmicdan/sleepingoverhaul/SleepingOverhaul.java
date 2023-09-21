package com.cosmicdan.sleepingoverhaul;

import com.cosmicdan.sleepingoverhaul.client.ClientState;
import com.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.cosmicdan.sleepingoverhaul.server.ServerState;
import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
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


    //public static void init() {
    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    public SleepingOverhaul() {
        // Register server/world config
        final Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        final ForgeConfigSpec serverConfigSpec = specPairServer.getRight();
        serverConfig = specPairServer.getLeft();
        ModConfigHelper.registerConfig(Type.SERVER, serverConfigSpec);

        serverState = new ServerState();
        if (Platform.getEnvironment() == Env.CLIENT)
            clientState = new ClientState();
        else
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

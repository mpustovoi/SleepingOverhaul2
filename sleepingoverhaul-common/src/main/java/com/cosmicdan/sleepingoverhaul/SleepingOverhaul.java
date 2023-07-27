package com.cosmicdan.sleepingoverhaul;

import com.cosmicdan.sleepingoverhaul.client.ClientState;
import com.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.cosmicdan.sleepingoverhaul.server.ServerState;
import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
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
    }

}

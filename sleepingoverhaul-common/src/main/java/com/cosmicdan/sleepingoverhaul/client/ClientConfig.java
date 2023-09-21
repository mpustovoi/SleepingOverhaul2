package com.cosmicdan.sleepingoverhaul.client;

import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientConfig {
    private static final String sectionGeneral = "clientGeneral";
    public final ForgeConfigSpec.EnumValue<TimelapseCameraType> timelapseCameraType;
    private static final String timelapseCameraTypeTxt = " The action to perform when all players are sleeping";

    public ClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push(sectionGeneral); // TODO: test translation key...?

        timelapseCameraType = builder
                .comment(timelapseCameraTypeTxt)
                //.translation("config.sleepingoverhaul.sleepAction") // TODO: what's the point of translation if only comment is used?
                .defineEnum("timelapseCameraType", TimelapseCameraType.SurfaceOrbit);
        builder.pop();
    }

    public enum TimelapseCameraType {
        SurfaceOrbit,
        SurfaceRotation,
        None,
    }
}

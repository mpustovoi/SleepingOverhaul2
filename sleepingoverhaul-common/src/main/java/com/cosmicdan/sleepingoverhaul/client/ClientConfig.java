package com.cosmicdan.sleepingoverhaul.client;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientConfig {
    private static final String sectionGeneral = "clientGeneral";
    public final ForgeConfigSpec.EnumValue<TimelapseCameraType> timelapseCameraType;
    private static final String timelapseCameraTypeTxt = " The action to perform when all players are sleeping";

    public ClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push(sectionGeneral);

        timelapseCameraType = builder
                .comment(timelapseCameraTypeTxt)
                .defineEnum("timelapseCameraType", TimelapseCameraType.SurfaceOrbit);
        builder.pop();
    }

    public enum TimelapseCameraType {
        SurfaceOrbit,
        SurfaceRotation,
        None,
    }
}

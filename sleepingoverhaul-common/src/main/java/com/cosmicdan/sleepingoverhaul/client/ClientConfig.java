package com.cosmicdan.sleepingoverhaul.client;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientConfig {
    private static final String sectionGeneral = "clientGeneral";
    private static final String sectionGeneralTxt = " General client-side settings.";
    public final ForgeConfigSpec.EnumValue<TimelapseCameraType> timelapseCameraType;
    private static final String timelapseCameraTypeTxt = " Camera effect to use under Timelapse";
    public final ForgeConfigSpec.IntValue timelapseDimValue;
    private static final String timelapseDimValueTxt = " Screen dim to use under Timelapse. The default value of 0 will remove the screen dim.";

    public ClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push(sectionGeneral).comment(sectionGeneralTxt);

        timelapseCameraType = builder
                .comment(timelapseCameraTypeTxt)
                .defineEnum("timelapseCameraType", TimelapseCameraType.SurfaceOrbit);

        timelapseDimValue = builder
                .comment(timelapseDimValueTxt)
                .defineInRange("timelapseDimValue", 0, 0, 100);

        builder.pop();
    }

    public enum TimelapseCameraType {
        SurfaceOrbit,
        SurfaceRotation,
        None,
    }
}

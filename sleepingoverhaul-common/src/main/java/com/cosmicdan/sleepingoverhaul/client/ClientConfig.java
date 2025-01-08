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
    public final ForgeConfigSpec.BooleanValue inBedChatFixes;
    private static final String inBedChatFixesTxt = " Enhances/fixes keyboard-navigation issues on the In-Bed chat screen. Summary:\n" +
            "     - Allows to re-focus the chat box by clicking on it;\n" +
            "     - Prevents arrow keys from changing focus away from chat box, use Ctrl+Tab to change focus instead;\n" +
            "     - Prevents ENTER from sending a chat message unless the chat box is actually focused;\n" +
            "     - Allows ENTER to actually work if one of the buttons is focused instead of the chat box.\n" +
            " You will probably want to keep this enabled because of the buttons we add to the screen, but it can be disabled in case there are mod conflicts.";

    public ClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push(sectionGeneral).comment(sectionGeneralTxt);

        timelapseCameraType = builder
                .comment(timelapseCameraTypeTxt)
                .defineEnum("timelapseCameraType", TimelapseCameraType.SurfaceOrbit);
        timelapseDimValue = builder
                .comment(timelapseDimValueTxt)
                .defineInRange("timelapseDimValue", 0, 0, 100);
        inBedChatFixes = builder
                .comment(inBedChatFixesTxt)
                .define("inBedChatFixes", true);
        builder.pop();
    }

    public enum TimelapseCameraType {
        SurfaceOrbit,
        SurfaceRotation,
        None,
    }
}

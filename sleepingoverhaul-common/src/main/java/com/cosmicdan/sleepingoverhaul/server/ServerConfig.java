package com.cosmicdan.sleepingoverhaul.server;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ServerConfig {
    private static final String sectionGeneral = "general";
    private static final String sectionGeneralTxt = " General features for the mod.";
    public final ForgeConfigSpec.EnumValue<SleepAction> sleepAction;
    private static final String sleepActionTxt = " The action to perform when all players are sleeping";
    public final ForgeConfigSpec.BooleanValue resetWeatherOnWake;
    private static final String resetWeatherOnWakeTxt = " Reset the weather if raining when players wake. Applies to Timelapse and SkipTime.";

    private static final String sectionTimelapse = "timelapse";
    private static final String sectionTimelapseTxt = " Features for the Timelapse sleepAction";
    public final ForgeConfigSpec.BooleanValue logTimelapsePerformanceStats;
    private static final String logTimelapsePerformanceStatsTxt = " If true, will performance stats will be logged on timelapse end (average TPS, total time, total ticks)";
    public final ForgeConfigSpec.BooleanValue disableNaturalSpawning;
    private static final String disableNaturalSpawningTxt = " If true, natural spawning will be disabled during timelapse.\n" +
            " Gives a minor speed boost.";
    public final ForgeConfigSpec.BooleanValue disableLivingEntityTravel;
    private static final String disableLivingEntityTravelTxt = " If true, LivingEntity type mobs can not travel during timelapse. Gives a minor speed boost.\n" +
            " Disabled by default since it could result in undesired loss, e.g. mobs drowning.\n" +
            " Note that this does NOT seem to include Villager movement.";

    private static final String sectionBedrest = "bedRest";
    private static final String sectionBedrestTxt = " Toggle and customize the Bed Rest feature here";
    public final ForgeConfigSpec.BooleanValue bedRestEnabled;
    private static final String bedRestEnabledTxt = " Allows players to rest in a bed without sleeping, adding a 'Sleep' button next to 'Leave Bed'";
    public final ForgeConfigSpec.BooleanValue bedRestOnEnter;
    private static final String bedRestOnEnterTxt = " Allows pressing Enter with no chat text to activate Sleep while resting. Requires the above bedRestEnabled to be true.";
    public final ForgeConfigSpec.IntValue bedRestScreenDimValue;
    private static final String bedRestScreenDimValueTxt = " How much to dim the screen when bed resting. A value below 10 or so will effectively disable the dim.";

    private static final String sectionBedEffectsAndDamage = "bedEffectsAndDamage";
    private static final String sectionBedEffectsAndDamageTxt = " Customize damage/effect modifiers while in bed. Note that the player will always be kicked out of bed if any damage is allowed to happen.";
    public final ForgeConfigSpec.EnumValue<AttackedWhileSleepingAction> sleepDirectDamageAction;
    private static final String sleepDirectDamageActionTxt = " The action to perform on a player if they are attacked with DIRECT damage (not a potion/effect) during bed rest or timelapse.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoPoison;
    private static final String bedEffectNoPoisonTxt = " If enabled, poison will not harm the player during bed rest or timelapse.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoWither;
    private static final String bedEffectNoWitherTxt = " If enabled, wither will not harm the player during bed rest or timelapse.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoHunger;
    private static final String bedEffectNoHungerTxt = " If enabled, hunger (effect) will not apply to the player during bed rest or timelapse.";



    public ServerConfig(final Builder builder) {
        builder.push(sectionGeneral).comment(sectionGeneralTxt);

        sleepAction = builder
                .comment(sleepActionTxt)
                .defineEnum("sleepAction", SleepAction.Timelapse);
        resetWeatherOnWake = builder
                .comment(resetWeatherOnWakeTxt)
                .define("resetWeatherOnWake", false);
        builder.pop();

        builder.push(sectionTimelapse).comment(sectionTimelapseTxt);
        logTimelapsePerformanceStats = builder
                .comment(logTimelapsePerformanceStatsTxt)
                .define("logTimelapsePerformanceStats", true);
        disableNaturalSpawning = builder
                .comment(disableNaturalSpawningTxt)
                .define("disableNaturalSpawning", true);
        disableLivingEntityTravel = builder
                .comment(disableLivingEntityTravelTxt)
                .define("disableLivingEntityTravel", false);
        builder.pop();

        builder.push(sectionBedrest).comment(sectionBedrestTxt);
        bedRestEnabled = builder
                .comment(bedRestEnabledTxt)
                .define("bedRestEnabled", true);
        bedRestOnEnter = builder
                .comment(bedRestOnEnterTxt)
                .define("bedRestWithChatEnter", true);
        bedRestScreenDimValue = builder
                .comment(bedRestScreenDimValueTxt)
                // must be between 1 and 98 because 0 means "awake", 100+ means "deep sleeping", and we need to reserve max+1 for the "isSleeping" multiplayer check
                .defineInRange("bedRestScreenDimValue", 30, 1, 98);
        builder.pop();

        builder.push(sectionBedEffectsAndDamage).comment(sectionBedEffectsAndDamageTxt);
        sleepDirectDamageAction = builder
                .comment(sleepDirectDamageActionTxt)
                .defineEnum("sleepDirectDamageAction", AttackedWhileSleepingAction.NoChange);
        bedEffectNoPoison = builder
                .comment(bedEffectNoPoisonTxt)
                .define("bedEffectNoPoison", true);
        bedEffectNoWither = builder
                .comment(bedEffectNoWitherTxt)
                .define("bedEffectNoWither", true);
        bedEffectNoHunger = builder
                .comment(bedEffectNoHungerTxt)
                .define("bedEffectNoHunger", true);
        builder.pop();
    }

    public enum SleepAction {
        Timelapse,
        SkipTime,
        Nothing
    }

    public enum AttackedWhileSleepingAction {
        NoChange,
        InstantKill,
        Invincible
    }
}

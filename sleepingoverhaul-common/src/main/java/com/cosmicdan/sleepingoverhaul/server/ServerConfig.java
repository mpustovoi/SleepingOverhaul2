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
    public final ForgeConfigSpec.EnumValue<AttackedWhileSleepingAction> sleepAttackedAction;
    private static final String sleepAttackedActionTxt = " The action to perform on a player if they are attacked during timelapse sleep, for damage sources that do *not* match any preventions below.";
    public final ForgeConfigSpec.BooleanValue sleepPreventMagicDamage;
    private static final String sleepPreventMagicDamageTxt = " If true, damage type of 'Magic' (pre-1.19.4) or 'Indirect' (1.19.4+) will not apply to players during timelapse. This includes DoT's like Poison.";
    public final ForgeConfigSpec.BooleanValue logTimelapsePerformanceStats;
    private static final String logTimelapsePerformanceStatsTxt = " If true, will performance stats will be logged on timelapse end (average TPS, total time, total ticks)";
    public final ForgeConfigSpec.BooleanValue disableNaturalSpawning;
    private static final String disableNaturalSpawningTxt = " If true, natural spawning will be disabled during timelapse.\n" +
            " Gives a minor speed boost.";
    public final ForgeConfigSpec.BooleanValue disableLivingEntityTravel;
    private static final String disableLivingEntityTravelTxt = " If true, LivingEntity type mobs can not travel during timelapse. Gives a minor speed boost.\n" +
            " Disabled by default since it could result in undesired loss, e.g. mobs drowning.\n" +
            " Note that this does NOT seem to include Villager movement.";
    // disabled for now since it needs extra work (prevent wakeup is always-on)
    //public final ForgeConfigSpec.BooleanValue timelapsePreventWakeup;
    //private static final String timelapsePreventWakeupTxt = "Prevents players from waking up during timelapse (players will only wake when morning arrives)";
    //public final ForgeConfigSpec.BooleanValue timelapsePreventMoving;
    //private static final String timelapsePreventMovingTxt = "Prevents players from moving during timelapse (in case vote passes with some players awake)";

    private static final String sectionBedrest = "bedRest";
    private static final String sectionBedrestTxt = " Toggle and customize the Bed Rest feature here";
    public final ForgeConfigSpec.BooleanValue bedRestEnabled;
    private static final String bedRestEnabledTxt = " Allows players to rest in a bed without sleeping, adding a 'Sleep' button next to 'Leave Bed'";
    public final ForgeConfigSpec.BooleanValue bedRestOnEnter;
    private static final String bedRestOnEnterTxt = " Allows pressing Enter with no chat text to activate Sleep while resting. Requires the above bedRestEnabled to be true.";
    public final ForgeConfigSpec.IntValue bedRestScreenDimValue;
    private static final String bedRestScreenDimValueTxt = " How much to dim the screen when bed resting. A value below 10 or so will effectively disable the dim.";

    //private static final String sectionRestrictions = "restrictions";
    //public final ForgeConfigSpec.BooleanValue dayCheckOverride;

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
        sleepAttackedAction = builder
                .comment(sleepAttackedActionTxt)
                .defineEnum("sleepAttackedAction", AttackedWhileSleepingAction.NoChange);
        sleepPreventMagicDamage = builder
                .comment(sleepPreventMagicDamageTxt)
                .define("sleepPreventMagicDamage", false);
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

package com.cosmicdan.sleepingoverhaul.server;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ServerConfig {
    private static final String sectionGeneral = "general";
    public final ForgeConfigSpec.EnumValue<SleepAction> sleepAction;
    private static final String sleepActionTxt = " The action to perform when all players are sleeping";

    private static final String sectionMorning = "morning";
    // TODO: Disabled because client (or player) has a "prevent sleep during day" hardcoded check
    //public final ForgeConfigSpec.IntValue morningHour;
    //private static final String morningHourTxt = " The hour of day to consider as 'morning' (for waking players)";
    //public final ForgeConfigSpec.IntValue morningMinute;
    //private static final String morningMinuteTxt = " The minute of day to consider as 'morning', past the hour (for waking players)";
    public final ForgeConfigSpec.BooleanValue resetWeatherOnWake;
    private static final String resetWeatherOnWakeTxt = " Reset the weather if raining when players wake. Applies to any bed and any SleepAction except Nothing.";

    private static final String sectionTimelapse = "timelapse";
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
    private static final String disableLivingEntityTravelTxt = " If true, LivingEntity type mobs will not travel during timelapse. Gives a minor speed boost.\n" +
            " Disabled by default since it could result in undesired loss, e.g. mobs drowning.\n" +
            " Note that this does NOT seem to include Villager movement.";
    // disabled for now since it needs extra work (prevent wakeup is always-on)
    //public final ForgeConfigSpec.BooleanValue timelapsePreventWakeup;
    //private static final String timelapsePreventWakeupTxt = "Prevents players from waking up during timelapse (players will only wake when morning arrives)";
    //public final ForgeConfigSpec.BooleanValue timelapsePreventMoving;
    //private static final String timelapsePreventMovingTxt = "Prevents players from moving during timelapse (in case vote passes with some players awake)";

    private static final String sectionBedrest = "bedRest";
    public final ForgeConfigSpec.BooleanValue bedRestEnabled;
    private static final String bedRestEnabledTxt = " Allows players to rest in a bed without sleeping, adding a 'Sleep' button next to 'Leave Bed'";
    public final ForgeConfigSpec.BooleanValue bedRestOnEnter;
    private static final String bedRestOnEnterTxt = " Allows pressing Enter with no chat text to activate Sleep while resting. Requires the above bedRestEnabled to be true.";

    //private static final String sectionRestrictions = "restrictions";
    //public final ForgeConfigSpec.BooleanValue dayCheckOverride;

    public ServerConfig(final Builder builder) {
        builder.push(sectionGeneral); // TODO: test translation key...?

        sleepAction = builder
                .comment(sleepActionTxt)
                //.translation("config.sleepingoverhaul.sleepAction") // TODO: what's the point of translation if only comment is used?
                .defineEnum("sleepAction", SleepAction.Timelapse);
        builder.pop();

        builder.push(sectionMorning);
        /*
        morningHour = builder
                .comment(morningHourTxt)
                .defineInRange("morningHour", 6, 0, 23);
        morningMinute = builder
                .comment(morningMinuteTxt)
                .defineInRange("morningMinute", 0, 0, 59);

         */
        resetWeatherOnWake = builder
                .comment(resetWeatherOnWakeTxt)
                .define("resetWeatherOnWake", false);
        builder.pop();


        builder.push(sectionTimelapse);
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
        /*
        timelapsePreventWakeup = builder
                .comment(timelapsePreventWakeupTxt)
                .define("timelapsePreventWakeup", true);
        timelapsePreventMoving = builder
                .comment(timelapsePreventMovingTxt)
                .define("timelapsePreventMoving", true);
         */
        builder.pop();

        builder.push(sectionBedrest);
        bedRestEnabled = builder
                .comment(bedRestEnabledTxt)
                .define("bedRestEnabled", true);
        bedRestOnEnter = builder
                .comment(bedRestOnEnterTxt)
                .define("bedRestWithChatEnter", true);
        builder.pop();
    }

    public enum SleepAction {
        Timelapse,
        SkipToDay,
        Nothing
    }

    public enum AttackedWhileSleepingAction {
        NoChange,
        InstantKill,
        Invincible
    }
}

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
    private static final String logTimelapsePerformanceStatsTxt = " If true, performance stats will be written to log on timelapse end (average TPS, total time, total ticks).";
    public final ForgeConfigSpec.BooleanValue noDamageToNonSleepers;
    public final ForgeConfigSpec.EnumValue<AttackedWhileSleepingAction> timelapseSleepersDirectDamageAction;
    private static final String timelapseSleepersDirectDamageActionTxt = " The action to perform on a SLEEPING if they are attacked with DIRECT damage (not a potion/effect) during timelapse. Doesn't apply to Bed Rest.";
    private static final String noDamageToNonSleepersTxt = " For multiplayer. If true, players NOT sleeping will be invincible during Timelapse.";
    public final ForgeConfigSpec.BooleanValue noMovementDuringTimelapse;
    private static final String noMovementDuringTimelapseTxt = " For multiplayer. If true, players NOT sleeping will be unable to move during Timelapse.";
    public final ForgeConfigSpec.BooleanValue disableNaturalSpawning;
    private static final String disableNaturalSpawningTxt = " If true, natural spawning will be disabled during timelapse.\n" +
            " Gives a minor speed boost.";
    public final ForgeConfigSpec.BooleanValue disableLivingEntityTravel;
    private static final String disableLivingEntityTravelTxt = " If true, LivingEntity type mobs can not travel during timelapse. Gives a minor speed boost.\n" +
            " Disabled by default since it could result in undesired loss, e.g. mobs drowning.\n" +
            " Note that this won't apply to players, see noMovementDuringTimelapse for stopping player movement.";

    private static final String sectionBedrest = "bedRest";
    private static final String sectionBedrestTxt = " Toggle and customize the Bed Rest feature here";
    public final ForgeConfigSpec.BooleanValue bedRestEnabled;
    private static final String bedRestEnabledTxt = " Allows players to rest in a bed without sleeping, adding a 'Sleep' button next to 'Leave Bed'";
    public final ForgeConfigSpec.BooleanValue bedRestOnEnter;
    private static final String bedRestOnEnterTxt = " Allows pressing Enter with no chat text to activate Sleep while resting. Requires the above bedRestEnabled to be true.";
    public final ForgeConfigSpec.IntValue bedRestScreenDimValue;
    private static final String bedRestScreenDimValueTxt = " How much to dim the screen when bed resting. A value below 10 or so will effectively disable the dim. This cannot be a client option because the value is used server-side too.";

    private static final String sectionBedEffectsAndDamage = "bedEffectsAndDamage";
    private static final String sectionBedEffectsAndDamageTxt = " Customize damage/effect modifiers while bed resting or timelapse. Note that the player will always be kicked out of bed if any damage is allowed to happen.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoPoison;
    private static final String bedEffectNoPoisonTxt = " If enabled, poison will not harm the player during bed rest or timelapse.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoWither;
    private static final String bedEffectNoWitherTxt = " If enabled, wither will not harm the player during bed rest or timelapse.";
    public final ForgeConfigSpec.BooleanValue bedEffectNoHunger;
    private static final String bedEffectNoHungerTxt = " If enabled, hunger (effect) will not apply to the player during bed rest or timelapse.";

    private static final String sectionFeatures = "features";
    private static final String sectionFeaturesTxt = " Other features";
    public final ForgeConfigSpec.BooleanValue featureAllowAnyDimension;
    private static final String featureAllowAnyDimensionTxt = " If enabled, allows resting/sleeping in any dimension. Will also disable explosion on use.\n" +
            " IMPORTANT - This will ONLY apply with vanilla and vanilla-like beds! Will not change spawn in other dimensions unless featureSetSpawnAnyDimension is also enabled.";
    public final ForgeConfigSpec.BooleanValue featureSetSpawnAnyDimension;
    private static final String featureSetSpawnAnyDimensionTxt = " If enabled, resting/sleeping in a non-overworld bed will ALSO set the spawn to that bed.\n" +
            " IMPORTANT - it will *overwrite* the overworld spawn point; it will NOT set per-dimension spawn points!\n" +
            " Again, this will probably only apply to vanilla and vanilla-like beds.";



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
        timelapseSleepersDirectDamageAction = builder
                .comment(timelapseSleepersDirectDamageActionTxt)
                .defineEnum("timelapseSleepersDirectDamageAction", AttackedWhileSleepingAction.NoChange);
        noDamageToNonSleepers = builder
                .comment(noDamageToNonSleepersTxt)
                .define("noDamageToNonSleepers", true);
        noMovementDuringTimelapse = builder
                .comment(noMovementDuringTimelapseTxt)
                .define("noMovementDuringTimelapse", true);
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

        builder.push(sectionFeatures).comment(sectionFeaturesTxt);
        featureAllowAnyDimension = builder
                .comment(featureAllowAnyDimensionTxt)
                .define("featureAllowAnyDimension", false);
        featureSetSpawnAnyDimension = builder
                .comment(featureSetSpawnAnyDimensionTxt)
                .define("featureSetSpawnAnyDimension", false);
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

package com.cosmicdan.sleepingoverhaul;

import net.minecraft.resources.ResourceLocation;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IClientState {
    ResourceLocation PACKET_SLEEPERROR_TIME = new ResourceLocation(SleepingOverhaul.MOD_ID, "sleep_error_time");
    ResourceLocation PACKET_TIMELAPSE_CHANGE = new ResourceLocation(SleepingOverhaul.MOD_ID, "timelapse_change");

    boolean isSleepButtonActive();

    <T> void leaveBedButtonAssign(T buttonRaw);

    <T> void sleepButtonAssign(T buttonRaw);

    void sleepButtonDisable();

    void doSleepButtonCooldown();

    void setTimelapseEnabled(boolean timelapseEnabled);

    int getTimelapseCinematicStage();

    void advanceTimelapseCinematicStage();

    void removeBedScreenButtons();
}

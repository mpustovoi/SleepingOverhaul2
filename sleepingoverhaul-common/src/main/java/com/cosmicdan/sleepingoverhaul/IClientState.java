package com.cosmicdan.sleepingoverhaul;

import net.minecraft.resources.ResourceLocation;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IClientState {
    boolean isSleepButtonActive();

    <T> void leaveBedButtonAssign(T buttonRaw);

    <T> void sleepButtonAssign(T buttonRaw);

    void sleepButtonCooldown();

    void setTimelapseEnabled(boolean timelapseEnabled);

    int getTimelapseCinematicStage();

    void advanceTimelapseCinematicStage();

    void removeBedScreenButtons();

    void onSleepingCheck(boolean isInBed);
}

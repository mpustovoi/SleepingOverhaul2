package com.cosmicdan.sleepingoverhaul;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IClientState {

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

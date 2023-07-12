package com.cosmicdan.sleepingoverhaul.server;

import com.cosmicdan.sleepingoverhaul.IClientState;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientStateDummy implements IClientState {

    @Override
    public boolean isSleepButtonActive() {
        return false;
    }

    @Override
    public <T> void leaveBedButtonAssign(T button) {}

    @Override
    public <T> void sleepButtonAssign(final T button) {}

    @Override
    public void sleepButtonDisable() {}

    @Override
    public void doSleepButtonCooldown() {}

    @Override
    public void setTimelapseEnabled(boolean timelapseEnabled) {

    }

    @Override
    public int getTimelapseCinematicStage() {
        return 0;
    }

    @Override
    public void advanceTimelapseCinematicStage() {}

    @Override
    public void removeBedScreenButtons() {}
}

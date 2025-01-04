package com.cosmicdan.sleepingoverhaul.server;

import com.cosmicdan.sleepingoverhaul.IClientState;
import net.minecraft.world.entity.player.Player;

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
    public void setTimelapseCamera(Player player, boolean timelapseEnabled) {}

    @Override
    public int getTimelapseCinematicStage() {
        return 0;
    }

    @Override
    public void advanceTimelapseCinematicStage() {}

    @Override
    public boolean isTimelapseCinematicActive() {return false;}

    @Override
    public void sleepButtonEnable(boolean enable) {}
}

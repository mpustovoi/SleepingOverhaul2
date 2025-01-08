package com.cosmicdan.sleepingoverhaul;

import net.minecraft.world.entity.player.Player;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IClientState {
    boolean isSleepButtonActive();

    <T> void leaveBedButtonAssign(T buttonRaw);

    <T> void sleepButtonAssign(T buttonRaw);

    void setTimelapseCamera(Player player, boolean timelapseEnabled);

    int getTimelapseCinematicStage();

    void advanceTimelapseCinematicStage();

    boolean isTimelapseCinematicActive();

    void sleepButtonEnable(boolean enable);

    void onClickSleep();
}

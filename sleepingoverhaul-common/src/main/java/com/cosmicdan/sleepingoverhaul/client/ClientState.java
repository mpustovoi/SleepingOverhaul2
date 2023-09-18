package com.cosmicdan.sleepingoverhaul.client;

import com.cosmicdan.sleepingoverhaul.IClientState;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientState implements IClientState {
    private Button leaveButton = null;
    private Button sleepButton = null;
    /**
     * 0 = Inactive/Stopped
     * 1 = Started
     * 2 = Playing
     * 3 = Ended
     */
    private int timelapseCinematicStage = 0;

    public ClientState() {
        NetworkManager.registerReceiver(Side.S2C, IClientState.PACKET_SLEEPERROR_TIME, this::recvSleepErrorTime);
        NetworkManager.registerReceiver(Side.S2C, IClientState.PACKET_TIMELAPSE_CHANGE, this::recvTimelapseChange);
    }

    private void recvSleepErrorTime(final FriendlyByteBuf buf, final PacketContext context) {
        context.getPlayer().displayClientMessage(BedSleepingProblem.NOT_POSSIBLE_NOW.getMessage(), true);
        doSleepButtonCooldown();
    }

    private void recvTimelapseChange(final FriendlyByteBuf buf, final PacketContext context) {
        //final Player player = context.getPlayer();
        final boolean timelapseEnabled = buf.readBoolean();
        setTimelapseEnabled(timelapseEnabled);
    }

    @Override
    public boolean isSleepButtonActive() {
        return (sleepButton != null) && sleepButton.isActive();
    }

    @Override
    public <T> void leaveBedButtonAssign(final T buttonRaw) {
        if (buttonRaw instanceof Button button) {
            leaveButton = button;
        }
    }

    @Override
    public <T> void sleepButtonAssign(final T buttonRaw) {
        if (buttonRaw instanceof Button button) {
            sleepButton = button;
        }
    }

    @Override
    public void sleepButtonDisable() {
        if (sleepButton != null)
            sleepButton.active = false;
    }

    /**
     * Just some simple client-side packet spam prevention
     */
    @Override
    public void doSleepButtonCooldown() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (sleepButton != null)
                    sleepButton.active = true;
            }
        }, 2000);
    }

    @Override
    public void setTimelapseEnabled(final boolean timelapseEnabled) {
        if (timelapseEnabled) {
            Minecraft.getInstance().gameRenderer.setPanoramicMode(true);
            if (timelapseCinematicStage == 0)
                timelapseCinematicStage = 1;
        } else {
            Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
            timelapseCinematicStage = 3;
        }

        // also update timelapse screen
        if (Minecraft.getInstance().screen != null) {
            if (Minecraft.getInstance().screen instanceof InBedChatScreen screenBedChat) {
                //((InBedChatScreenProxy) screenBedChat).onTimelapseChange(timelapseEnabled);
                if (timelapseEnabled) {
                    // timelapse started, remove the buttons
                    removeBedScreenButtons();
                } else {
                    // timelapse ended, switch to regular chat screen if there is text still in chat box
                    screenBedChat.onPlayerWokeUp();
                }
            }
        }
    }

    @Override
    public int getTimelapseCinematicStage() {
        return timelapseCinematicStage;
    }

    @Override
    public void advanceTimelapseCinematicStage() {
        if (timelapseCinematicStage == 3)
            timelapseCinematicStage = 0;
        else
            timelapseCinematicStage++;
    }

    @Override
    public void removeBedScreenButtons() {
        if (leaveButton != null)
            leaveButton.visible = false;
        if (sleepButton != null)
            sleepButton.visible = false;
    }

    @Override
    public void onSleepingCheck(final boolean isInBed) {
        // if player is not in bed, ensure cinematic is not active
        if (!isInBed && (timelapseCinematicStage != 0))
            timelapseCinematicStage = 0;
    }
}

package com.cosmicdan.sleepingoverhaul.client;

import com.cosmicdan.sleepingoverhaul.IClientState;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        NetworkManager.registerReceiver(Side.S2C, SleepingOverhaul.PACKET_TIMELAPSE_CHANGE, this::recvTimelapseChange);
        NetworkManager.registerReceiver(Side.S2C, SleepingOverhaul.PACKET_TRY_REALLY_SLEEPING, this::recvTrySleepBounce);
    }

    private void recvTimelapseChange(final FriendlyByteBuf buf, final PacketContext context) {
        //final Player player = context.getPlayer();
        final boolean timelapseEnabled = buf.readBoolean();
        setTimelapseCamera(context.getPlayer(), timelapseEnabled);
    }

    private void recvTrySleepBounce(final FriendlyByteBuf buf, final PacketContext context) {
        final Player player = context.getPlayer();
        boolean reallySleeping = buf.readBoolean();
        if (!reallySleeping) {
            player.displayClientMessage(Component.translatable("gui.sleepingoverhaul.sleepNotPossibleNow"), true);
            ((PlayerMixinProxy) player).setReallySleeping(false);
            // re-enable sleep button after 2 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sleepButtonEnable(true);
                }
            }, 2000);
        }
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
    public void setTimelapseCamera(Player player, final boolean timelapseEnabled) {
        if (timelapseEnabled && player.isSleeping()) { //
            Minecraft.getInstance().gameRenderer.setPanoramicMode(true);
            if (timelapseCinematicStage == 0)
                timelapseCinematicStage = 1;
        } else {
            Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
            timelapseCinematicStage = 3;
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
    public boolean isTimelapseCinematicActive() {
        return timelapseCinematicStage != 0;
    }

    @Override
    public void sleepButtonEnable(boolean enable) {
        if (sleepButton != null)
            sleepButton.active = enable;
    }
}

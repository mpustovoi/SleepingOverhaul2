package com.cosmicdan.sleepingoverhaul.mixin.injection_old;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketLstnrMixin {
    // TODO: Add isReallySleeping check to STOP_SLEEPING clause on packet receive
    //       Not a huge deal but adding this will prevent "hacked" clients from waking during timelapse
}

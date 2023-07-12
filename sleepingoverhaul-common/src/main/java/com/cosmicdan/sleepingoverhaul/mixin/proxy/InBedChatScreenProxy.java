package com.cosmicdan.sleepingoverhaul.mixin.proxy;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface InBedChatScreenProxy {
    EditBox getInput();

    //void onTimelapseChange(final boolean timelapseEnabled);
}

package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

class BedRestMixinsCommonClient {}

@Mixin(InBedChatScreen.class)
abstract class BedRestMixinsCommonClientInBedChatScreen extends ChatScreen {
    protected BedRestMixinsCommonClientInBedChatScreen(final String string) {
        super(string);
    }

    /**
     * Support for requesting Sleep by pressing ENTER when there is no chat input
     */
    @Inject(
            method = "keyPressed(III)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/components/EditBox.setValue (Ljava/lang/String;)V"),
            require = 1, allow = 1
    )
    public final void onClearChatEntry(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (SleepingOverhaul.serverConfig.bedRestOnEnter.get()) {
            if (SleepingOverhaul.clientState.isSleepButtonActive()) {
                if (input.getValue().isEmpty()) {
                    // try to sleep if ENTER was pressed and there was no input at all (not just whitespace)
                    onClickSleep();
                }
            }
        }
    }

    /**
     * Adds the "Sleep" button on bed chat screen
     */
    @WrapOperation(
            method = "init()V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screens/InBedChatScreen.addRenderableWidget (Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"),
            require = 1, allow = 1
    )
    public final GuiEventListener onInitAddWidget(final InBedChatScreen self, final GuiEventListener guiEventListener, Operation<GuiEventListener> original) {
        if (guiEventListener instanceof Button buttonLeave) {
            if (SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
                // reduce buttonLeave width and move 5px left
                buttonLeave.setWidth(100);
                buttonLeave.setX(buttonLeave.getX() - 5);
                // add our new "Sleep" button with same dimensions, 5px to the right of screen center

                final Button sleepButton = new Button.Builder(
                        Component.translatable("gui.sleepingoverhaul.sleepButton"),
                        (Button button) -> onClickSleep()
                ).bounds((width / 2) + 5, buttonLeave.getY(), 100, 20
                ).build();

                addRenderableWidget(sleepButton);
                SleepingOverhaul.clientState.sleepButtonAssign(sleepButton);
            }
            // keep a reference to buttonLeave
            SleepingOverhaul.clientState.leaveBedButtonAssign(buttonLeave);
            // return the buttonLeave result since it's required, InBedChatScreen doesn't use it though
            return addRenderableWidget(buttonLeave);
        }
        throw new RuntimeException("onInitAddWidget was not called with a button, eh? Possible mod conflict? Tell CosmicDan to fix this, be sure to give your full mod list!");
    }

    /**
     * Action handler for Sleep
     */
    private void onClickSleep() {
        SleepingOverhaul.clientState.sleepButtonEnable(false);
        final LocalPlayer player = Minecraft.getInstance().player;
        // Assume really-sleeping works so the client can update immediately; the server will bounce back if it fails.
        ((PlayerMixinProxy) player).setReallySleeping(true);
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        NetworkManager.sendToServer(SleepingOverhaul.PACKET_TRY_REALLY_SLEEPING, buf);
    }
}

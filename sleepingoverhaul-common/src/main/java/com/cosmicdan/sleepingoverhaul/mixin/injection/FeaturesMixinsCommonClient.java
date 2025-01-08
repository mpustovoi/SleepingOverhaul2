package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.ChatScreenProxy;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class FeaturesMixinsCommonClient {}

@Mixin(ChatScreen.class)
abstract class FeaturesMixinsCommonClientChatScreen extends Screen implements ChatScreenProxy {
    @Shadow
    protected EditBox input;

    protected FeaturesMixinsCommonClientChatScreen(Component title) {
        super(title);
    }

    /**
     * For inBedChatFixes; allows the chat box to lose focus
     */
    @WrapOperation(
            method = "init",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setCanLoseFocus(Z)V")
    )
    private void onInit(EditBox instance, boolean canLoseFocus, Operation<Void> original) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen) {
            input.setCanLoseFocus(true);
        } else {
            original.call(instance, canLoseFocus);
        }
    }

    /**
     * For inBedChatFixes; allows clicking the Chat Box to focus it
     */
    @Inject(
            method = "mouseClicked",
            at = @At("HEAD")
    )
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen) {
            if (input.mouseClicked(mouseX, mouseY, button)) {
                input.setFocused(true);
                this.setFocused(input);
            }
        }
    }

    @Override
    public EditBox getInput() {
        return input;
    }
}

@Mixin(InBedChatScreen.class)
abstract class FeaturesMixinsCommonClientInBedChatScreen extends ChatScreen {
    @Shadow
    protected abstract void sendWakeUp();

    public FeaturesMixinsCommonClientInBedChatScreen(String initial) {
        super(initial);
    }

    /**
     * For inBedChatFixes:
     * - Only sends the message if chat box is actually focused;
     * - //Prevents arrow keys from changing focus;
     * - Enables Ctrl+Tab to switch focus to/from the chat box;
     * - Enables ENTER to actually work on buttons (if chat box is NOT focused)
     *
     */
    @WrapMethod(
            method = "keyPressed"
    )
    private boolean onKeyPressed(int keyCode, int scanCode, int modifiers, Operation<Boolean> original) {
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.sendWakeUp();
            }
            if (input.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == GLFW.GLFW_MOD_CONTROL) {
                    // Ctrl+Tab was pressed, simulate focus-up
                    ComponentPath nextFocusPath = super.nextFocusPath(new FocusNavigationEvent.ArrowNavigation(ScreenDirection.UP));
                    if (nextFocusPath != null)
                        changeFocus(nextFocusPath);
                    return true;
                } else if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
                    // chat not allowed, do nothing
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    // ENTER was pressed, handle chat
                    if (this.handleChatInput(this.input.getValue(), true)) {
                        // handle bedRestOnEnter config, because the hook from BedRestMixinsCommonClientInBedChatScreen is effectively replaced with this mixin
                        if (input.getValue().isEmpty() && SleepingOverhaul.clientConfig.bedRestOnEnter.get())
                            SleepingOverhaul.clientState.onClickSleep();
                        this.minecraft.setScreen(null);
                        this.input.setValue("");
                        this.minecraft.gui.getChat().resetChatScroll();
                    }
                    return true;
                }
            } else {
                if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == GLFW.GLFW_MOD_CONTROL) {
                    // input is not focused but Ctrl+Tab was pressed, force focus to input box
                    // This is useful because down-arrow won't work if command suggestion popup is active
                    input.setFocused(true);
                    this.setFocused(input);
                    return true;
                }
            }
            // if input is not focused or the keyCode wasn't enter, defer to super (ChatScreen).
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else
            return original.call(keyCode, scanCode, modifiers);
    }
}

@Mixin(Screen.class)
abstract class FeaturesMixinsCommonClientScreen {
    /**
     * For inBedChatFixes; blocks Tab/Arrow navigation if input is focused
     */
    @WrapOperation(
            method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;changeFocus(Lnet/minecraft/client/gui/ComponentPath;)V")
    )
    private void onKeyPressedChangeFocus(Screen instance, ComponentPath path, Operation<Void> original) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen inBedChatScreen) {
            if (((ChatScreenProxy) inBedChatScreen).getInput().isFocused()) {
                // do nothing
                return;
            }
        }
        original.call(instance, path);
    }
}

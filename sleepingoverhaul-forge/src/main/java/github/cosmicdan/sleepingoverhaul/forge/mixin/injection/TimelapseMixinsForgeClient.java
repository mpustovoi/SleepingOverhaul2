package github.cosmicdan.sleepingoverhaul.forge.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class TimelapseMixinsForgeClient {}

@Mixin(ForgeGui.class)
abstract class TimelapseMixinsForgeClientForgeGui {
    @WrapOperation(
            method = "renderSleepFade",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I")
    )
    private int onRenderGetSleepTimer(LocalPlayer instance, Operation<Integer> original) {
        if (SleepingOverhaul.serverConfig.sleepAction.get() == ServerConfig.SleepAction.Timelapse) {
            if (SleepingOverhaul.clientState.isTimelapseCinematicActive()) {
                // Use user-specified screen dim value
                return SleepingOverhaul.clientConfig.timelapseDimValue.get();
            }
        }
        return original.call(instance);
    }
}

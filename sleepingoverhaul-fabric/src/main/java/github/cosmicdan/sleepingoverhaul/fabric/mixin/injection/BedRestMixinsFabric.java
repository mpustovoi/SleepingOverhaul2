package github.cosmicdan.sleepingoverhaul.fabric.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class BedRestMixinsFabric {}

@Mixin(ServerPlayer.class)
abstract class BedRestMixinsFabricServerPlayer {

    /**
     * For Bed Rest, remove isDay check during tick. We perform the check later in ServerState#onReallySleepingRecv
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z")
    )
    private boolean onIsDayCheck(Level instance, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return false;
        else
            return original.call(instance);
    }
}

@Mixin(Player.class)
abstract class BedRestMixinsFabricPlayer {
    /**
     * For Bed Rest, remove isDay check during tick. We perform the check later in ServerState#onReallySleepingRecv
     */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z")
    )
    private boolean onIsDayCheck(Level instance, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return false; // We check for correct sleeping time later in ServerState#onReallySleepingRecv
        else
            return original.call(instance);
    }
}

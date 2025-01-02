package com.cosmicdan.sleepingoverhaul.mixin.injection_old;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hook to prevent living travel during timelapse option (for increased timelapse speed)
 * @author Daniel 'CosmicDan' Connolly
 */
@SuppressWarnings("MethodMayBeStatic")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1, allow = 1
    )
    public final void onTravel(Vec3 vec3, CallbackInfo ci) {
        if (SleepingOverhaul.serverState.shouldPreventLivingTravel())
            ci.cancel();
    }
}

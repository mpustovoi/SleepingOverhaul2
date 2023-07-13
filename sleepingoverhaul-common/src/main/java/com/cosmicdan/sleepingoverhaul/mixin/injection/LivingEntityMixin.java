package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onTravel(Vec3 vec3, CallbackInfo ci) {
        if (SleepingOverhaul.CONFIG_SERVER.disableLivingEntityTravel.get() && (SleepingOverhaul.timelapseEnd > 0))
            ci.cancel();
    }
}

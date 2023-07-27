package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles bedRest feature
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerMixinProxy {
    private boolean reallySleeping = false;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }


    @Redirect(method = "tick()V", at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/world/entity/player/Player.isSleeping ()Z"))
    private boolean isSleepingCheck(final Player player) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return isSleeping() && reallySleeping; // only do reallySleeping check if bed rest is actually enabled
        else
            return isSleeping();
    }

    @Inject(at = @At("TAIL"), method = "stopSleepInBed(ZZ)V")
    private void stopSleepInBed(boolean resetSleepCounter, boolean updateServer, CallbackInfo ci) {
        // this is called on both server and client side, no need for a custom packet here
        reallySleeping = false;
    }

    // Called from our custom C2S packet when the player presses Sleep
    @Override
    public final void setReallySleeping(final boolean isReallySleeping) {
        reallySleeping = isReallySleeping;
    }

}

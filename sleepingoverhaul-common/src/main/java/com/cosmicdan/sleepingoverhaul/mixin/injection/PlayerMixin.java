package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
 * Support for Bed Rest option
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerMixinProxy {
    private boolean reallySleeping = false;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
            method = "tick()V",
            at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/world/entity/player/Player.isSleeping ()Z"),
            require = 1, allow = 1
    )
    // TODO: Doesn't handle the second isSleeping call, related to "time since rest" player stat... important?
    private boolean isSleepingCheck(final Player self, Operation<Boolean> original) {
        final boolean isInBed = isSleeping();
        SleepingOverhaul.clientState.onSleepingCheck(isInBed);
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            return isInBed && reallySleeping; // only do reallySleeping check if bed rest is actually enabled
        else
            return isInBed;
    }

    @Inject(
            method = "stopSleepInBed(ZZ)V",
            at = @At("TAIL"),
            require = 1, allow = 1
    )
    private void stopSleepInBed(boolean resetSleepCounter, boolean updateServer, CallbackInfo ci) {
        // this is called on both server and client side, no need for a custom packet here
        reallySleeping = false;
    }

    /**
     * Called from our custom C2S packet when the player presses Sleep
     */
    @Override
    public final void setReallySleeping(final boolean isReallySleeping) {
        reallySleeping = isReallySleeping;
    }

}

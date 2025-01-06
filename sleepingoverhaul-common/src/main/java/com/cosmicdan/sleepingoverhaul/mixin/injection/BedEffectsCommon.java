package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class BedEffectsCommon {}

/**
 * Hooks for preventing Poison/Wither/Hunger while in bed if enabled in config
 */
@Mixin(MobEffect.class)
abstract class BedEffectsCommonMobEffect {

    @ModifyExpressionValue(
            method = "applyEffectTick",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/effect/MobEffects;POISON:Lnet/minecraft/world/effect/MobEffect;")
    )
    private MobEffect onIsEffectPoison(MobEffect original, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (SleepingOverhaul.serverConfig.bedEffectNoPoison.get()) {
            if (livingEntity instanceof Player && livingEntity.isSleeping())
                return null;
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "applyEffectTick",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/effect/MobEffects;WITHER:Lnet/minecraft/world/effect/MobEffect;")
    )
    private MobEffect onIsEffectWither(MobEffect original, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (SleepingOverhaul.serverConfig.bedEffectNoWither.get()) {
            if (livingEntity instanceof Player && livingEntity.isSleeping())
                return null;
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "applyEffectTick",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/effect/MobEffects;HUNGER:Lnet/minecraft/world/effect/MobEffect;")
    )
    private MobEffect onIsEffectHunger(MobEffect original, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (SleepingOverhaul.serverConfig.bedEffectNoHunger.get()) {
            if (livingEntity instanceof Player && livingEntity.isSleeping())
                return null;
        }
        return original;
    }
}

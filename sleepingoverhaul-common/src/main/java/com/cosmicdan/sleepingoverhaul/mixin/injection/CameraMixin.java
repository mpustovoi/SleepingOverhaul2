package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    private int cinematicSubstage = 0;

    @Shadow protected abstract void move(double d, double e, double f);

    @Shadow protected abstract double getMaxZoom(double d);

    @Shadow protected abstract void setRotation(float f, float g);

    @Inject(
            method = "setup",
            at = @At("TAIL"))
    private void afterCameraSetup(BlockGetter levelIn, Entity cameraEntity, boolean isThirdPerson, boolean isMirrored, float partialTicks, CallbackInfo ci) {
        //System.out.println(cameraEntity.getYRot();
        final int cineStage = SleepingOverhaul.CLIENT_STATE.getTimelapseCinematicStage();
        if (cineStage == 1) {
            // just skip to next cine stage for now
            SleepingOverhaul.CLIENT_STATE.advanceTimelapseCinematicStage();
        } else if (cineStage == 2) {
            if (levelIn instanceof ClientLevel level) {
                // level.getSunAngle is weird, so we calculate it ourselves
                final float timeOfDayAsFraction = (level.getDayTime() % 24000L) / 24000.0f;
                // rotate first
                setRotation((timeOfDayAsFraction * 360.0f * 2.0f) - 90, 0.0f);
                // move camera back and up
                move(-getMaxZoom(6.0), 3.0, 0.0);
                // finally set FoV 90
                Minecraft.getInstance().gameRenderer.setPanoramicMode(true);
            }
        } else if (cineStage == 3) {
            // timelapse was playing but is now ended
            Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
            // just go back to stopped cine stage for now
            SleepingOverhaul.CLIENT_STATE.advanceTimelapseCinematicStage();
        }
    }
}

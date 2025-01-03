package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.client.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class TimelapseMixinsCommonClient {}

@Mixin(Camera.class)
abstract class TimelapseMixinsCommonClientCamera {
    @Shadow
    protected abstract void move(double d, double e, double f);

    @Shadow protected abstract double getMaxZoom(double d); // reminder: used for chase cam collision

    @Shadow protected abstract void setRotation(float f, float g);

    @Shadow public abstract BlockPos getBlockPosition();

    @Shadow protected abstract void setPosition(Vec3 vec3);

    @Shadow private Vec3 position;

    private double previousMaxHeight = -1.0;

    @SuppressWarnings("MethodWithTooManyParameters")
    @Inject(
            method = "setup",
            at = @At("TAIL"),
            require = 1, allow = 1
    )
    private void afterCameraSetup(final BlockGetter levelIn, final Entity cameraEntity, final boolean isThirdPerson, final boolean isMirrored, final float partialTicks, final CallbackInfo ci) {
        final int cineStage = SleepingOverhaul.clientState.getTimelapseCinematicStage();
        if (cineStage == 1) {
            // just skip to next cine stage for now (only did this for a transition animation originally but meh)
            Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
            SleepingOverhaul.clientState.advanceTimelapseCinematicStage();
            previousMaxHeight = -1.0;
        } else if (cineStage == 2) {
            final ClientConfig.TimelapseCameraType timelapseCameraType = SleepingOverhaul.clientConfig.timelapseCameraType.get();
            if ((timelapseCameraType != ClientConfig.TimelapseCameraType.None) && (levelIn instanceof ClientLevel level)) {
                // level.getSunAngle is weird, so we calculate it ourselves
                final float timeOfDayAsFraction = (level.getDayTime() % 24000L) / 24000.0f;

                // rotate camera first
                setRotation((timeOfDayAsFraction * 360.0f * 2.0f) - 90, 0.0f);

                if (timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceOrbit) {
                    // SurfaceOrbit: Move camera back a bit
                    move(-6.0, 1.0, 0.0);
                }
                if ((timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceOrbit) || (timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceRotation)) {
                    // SurfaceOrbit *or* SurfaceRotation: move camera to surface block + 3 (unless lower than previous surface height in the current cine)
                    final BlockPos topmostPosition = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, getBlockPosition()).above(3);
                    if (topmostPosition.getY() > previousMaxHeight)
                        previousMaxHeight = topmostPosition.getY();
                    setPosition(new Vec3(position.x(), previousMaxHeight, position.z()));
                }
            }
        } else if (cineStage == 3) {
            previousMaxHeight = -1.0;
            Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
            SleepingOverhaul.clientState.advanceTimelapseCinematicStage();
        }
    }
}

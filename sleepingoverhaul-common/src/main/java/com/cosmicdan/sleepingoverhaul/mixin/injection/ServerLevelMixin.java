package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.server.ServerState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Various hooks:
 * - Controlling custom "when all players are asleep" logic
 * - Whether to skip to day or not
 * - Whether to wake all players wake the next morning
 * - Whether to clear weather after skip-to-day
 *
 * TODO: Javadoc for each method
 *
 * @author Daniel 'CosmicDan' Connolly
 */
@SuppressWarnings("MethodMayBeStatic")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements WorldGenLevel {
    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l);
    }

    @Shadow protected abstract void wakeUpAllPlayers();

    private static final Logger LOGGER = LogUtils.getLogger();

    @WrapOperation(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z"),
            require = 1, allow = 1
    )
    public final boolean shouldGetBooleanGameRuleFirst(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original) {
        if (key.equals(GameRules.RULE_DAYLIGHT)) {
            if (gameRules.getBoolean(key)) {
                switch (SleepingOverhaul.serverConfig.sleepAction.get()) {
                    case Timelapse -> {
                        final boolean timelapseStopped = !SleepingOverhaul.serverState.tickTimelapse(getLevel());
                        if (timelapseStopped) {
                            wakeUpAllPlayers();
                            resetWeatherCycleIfNeeded();
                        }
                    }
                    case SkipToDay -> {
                        setDayTime(ServerState.getNextMorning(getLevel()));
                        wakeUpAllPlayers();
                        resetWeatherCycleIfNeeded();
                    }
                    case Nothing -> {
                        // do nothing
                    }
                }
            }
            return false; // never do vanilla behaviour, we reproduce it above
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the first GameRules.getBoolean call was not RULE_DAYLIGHT! Mod conflict...?");
        }
    }

    public final void resetWeatherCycleIfNeeded() {
        if (getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && isRaining() && SleepingOverhaul.serverConfig.morningResetWeather.get())
            resetWeatherCycle();
    }

    @WrapOperation(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", ordinal = 1, target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z"),
            require = 1, allow = 1
    )
    public final boolean shouldGetBooleanGameRuleSecond(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original) {
        if (key.equals(GameRules.RULE_WEATHER_CYCLE)) {
            return SleepingOverhaul.serverConfig.morningResetWeather.get();
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the second GameRules.getBoolean call was not RULE_WEATHER_CYCLE! Mod conflict...?");
        }
    }

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSkyBrightness()V"),
            require = 1, allow = 1
    )
    public final void onUpdateSkyBrightness(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        SleepingOverhaul.serverState.onBeforeTickTime(getLevel());
    }

    // INVOKEVIRTUAL net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V"),
            require = 1, allow = 1
    )
    public final void onWakeUpAllPlayers(ServerLevel self) {
        // Do nothing always, we wake players ourselves at the appropriate time
        return;
    }

    // INVOKEVIRTUAL net/minecraft/server/level/ServerLevel.resetWeatherCycle ()V
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.resetWeatherCycle ()V"),
            require = 1, allow = 1
    )
    public final void onResetWeatherCycle(ServerLevel self) {
        // Do nothing always, we reset weather ourselves at the appropriate time
        return;
    }

    @Shadow
    private void resetWeatherCycle() { throw new AssertionError(); }

    @Shadow
    public void setDayTime(final long dayTime) { throw new AssertionError(); }

    @Shadow @Final
    EntityTickList entityTickList;

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow public abstract ServerLevel getLevel();

    /*
    // GETFIELD net/minecraft/server/level/ServerLevel.entityTickList : Lnet/minecraft/world/level/entity/EntityTickList;
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;entityTickList:Lnet/minecraft/world/level/entity/EntityTickList;")
    )
    private EntityTickList onGetEntityTickList(final ServerLevel self) {
        // TODO: return an empty list if timelapse active and "skip entity ticks" is configured
        return entityTickList;
    }
    */
}

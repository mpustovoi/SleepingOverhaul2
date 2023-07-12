package com.cosmicdan.sleepingoverhaul.mixin.injection;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.mojang.logging.LogUtils;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
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
 * Responsible for adjusting the "when all players are asleep" logic on the server.
 *
 * Includes hooks for adjusting:
 * - Whether to skip to day or not
 * - Whether to wake all players at the next morning
 * - Whether to clear weather after skip-to-day
 *
 * @author Daniel 'CosmicDan' Connolly
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements WorldGenLevel {
    @Shadow protected abstract void wakeUpAllPlayers();

    private static final Logger LOGGER = LogUtils.getLogger();

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l);
    }

    // INVOKEVIRTUAL net/minecraft/server/level/ServerLevel.setDayTime (J)V
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z", ordinal = 0)
    )
    private boolean onGetBooleanGamerule(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key) {
        if (key.equals(GameRules.RULE_DAYLIGHT)) {
            if (gameRules.getBoolean(key)) {
                switch (SleepingOverhaul.CONFIG_SERVER.sleepAction.get()) {
                    case Timelapse -> {
                        if (SleepingOverhaul.timelapseEnd == -1) {
                            SleepingOverhaul.timelapseEnd = getNextMorning();
                            notifyPlayersTimelapseChange(true);
                        } else if (SleepingOverhaul.timelapseEnd == -2) {
                            SleepingOverhaul.timelapseEnd = -1;
                            wakeUpAllPlayers();
                            resetWeatherCycleIfNeeded();
                            notifyPlayersTimelapseChange(false);
                        }
                    }
                    case SkipToDay -> {
                        setDayTime(getNextMorning());
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

    private void notifyPlayersTimelapseChange(final boolean timelapseActive) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(timelapseActive);
        NetworkManager.sendToPlayers(players, SleepingOverhaul.PACKET_TIMELAPSE_CHANGE, buf);
    }

    private void resetWeatherCycleIfNeeded() {
        if (getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && isRaining() && SleepingOverhaul.CONFIG_SERVER.morningResetWeather.get())
            resetWeatherCycle();
    }

    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/GameRules.getBoolean (Lnet/minecraft/world/level/GameRules$Key;)Z", ordinal = 1)
    )
    private boolean onGetBooleanGameruleTwo(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key) {
        if (key.equals(GameRules.RULE_WEATHER_CYCLE)) {
            return SleepingOverhaul.CONFIG_SERVER.morningResetWeather.get();
        } else {
            throw new RuntimeException("Unexpected Minecraft code; the second GameRules.getBoolean call was not RULE_WEATHER_CYCLE! Mod conflict...?");
        }
    }

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSkyBrightness()V"))
    private void onUpdateSkyBrightness(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        //System.out.println(getDayTime() + ">=" + SleepingOverhaul.timelapseEnd);
        if (SleepingOverhaul.timelapseEnd > 0) {
            // timelapse is currently active
            if (getDayTime() >= SleepingOverhaul.timelapseEnd) {
                SleepingOverhaul.timelapseEnd = -2;
            }
        }


    }

    private long getNextMorning() {
        final long oneDayAhead = levelData.getDayTime() + TICKS_PER_DAY;
        final long nextMorning = oneDayAhead - (oneDayAhead % TICKS_PER_DAY); // next multiple of 24000 = 06:00
        // calculate the tick offset according to config
        /*
        final int nextMorningHourOffset = (-6 + SleepingOverhaul.CONFIG_SERVER.morningHour.get()) * 1000;
        final int nextMorningMinuteOffset = Math.round(SleepingOverhaul.CONFIG_SERVER.morningMinute.get() * (1000 / 60.0f));
        final int nextMorningOffset = nextMorningHourOffset + nextMorningMinuteOffset;
        return nextMorning + nextMorningOffset;
         */
        return nextMorning;
    }

    // INVOKEVIRTUAL net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V")
    )
    private void onWakeUpAllPlayers(ServerLevel self) {
        // Do nothing always, we wake players ourselves at the appropriate time
    }

    // INVOKEVIRTUAL net/minecraft/server/level/ServerLevel.resetWeatherCycle ()V
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.resetWeatherCycle ()V")
    )
    private void onResetWeatherCycle(ServerLevel self) {
        // Do nothing always, we reset weather ourselves at the appropriate time
    }

    @Shadow
    private void resetWeatherCycle() { throw new AssertionError(); }

    @Shadow
    public void setDayTime(final long dayTime) { throw new AssertionError(); }

    @Shadow @Final
    EntityTickList entityTickList;

    @Shadow @Final private List<ServerPlayer> players;

    // GETFIELD net/minecraft/server/level/ServerLevel.entityTickList : Lnet/minecraft/world/level/entity/EntityTickList;
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;entityTickList:Lnet/minecraft/world/level/entity/EntityTickList;")
    )
    private EntityTickList onGetEntityTickList(final ServerLevel self) {
        // TODO: return an empty list if timelapse active and "skip entity ticks" is configured
        return entityTickList;
    }

}

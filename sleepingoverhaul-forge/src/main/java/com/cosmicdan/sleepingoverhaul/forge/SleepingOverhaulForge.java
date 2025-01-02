package com.cosmicdan.sleepingoverhaul.forge;

import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SleepingOverhaul.MOD_ID)
public class SleepingOverhaulForge {
    private final SleepingOverhaul INSTANCE;

    public SleepingOverhaulForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(SleepingOverhaul.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        INSTANCE = new SleepingOverhaul();

        // register Forge-specific events
        MinecraftForge.EVENT_BUS.register(this);
    }

    //@SubscribeEvent
    //public void onSleepingTimeCheck(final SleepingTimeCheckEvent event) {
        //SleepingOverhaul.LOGGER.info("~ Fired: SleepingTimeCheckEvent");
    //}

    //@SubscribeEvent
    //public void onPlayerSleepInBedEvent(final PlayerSleepInBedEvent event) {
    //    SleepingOverhaul.LOGGER.info("~ Fired: PlayerSleepInBedEvent");
    //}

    @SubscribeEvent
    public void onSleepFinishedTimeEvent(final SleepFinishedTimeEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: SleepFinishedTimeEvent");
    }

    /**
     * Only used for performance counting stats right now
     */
    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        //if (event.phase == TickEvent.Phase.END)
        //    SleepingOverhaul.serverState.onServerTickPost();
    }
}

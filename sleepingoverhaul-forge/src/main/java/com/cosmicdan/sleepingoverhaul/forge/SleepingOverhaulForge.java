package com.cosmicdan.sleepingoverhaul.forge;

import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import dev.architectury.platform.forge.EventBuses;
import com.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
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

    /**
     * Allows laying in bed at any time. We re-implement the sleep time check elsewhere.
     * @param event
     */
    @SubscribeEvent
    public void onSleepingTimeCheck(final SleepingTimeCheckEvent event) {
        if (SleepingOverhaul.serverConfig.bedRestEnabled.get())
            event.setResult(Result.ALLOW);
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            SleepingOverhaul.serverState.onServerTickPost();
    }
}

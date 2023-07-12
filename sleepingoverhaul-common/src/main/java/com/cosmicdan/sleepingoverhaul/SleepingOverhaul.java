package com.cosmicdan.sleepingoverhaul;

import com.cosmicdan.sleepingoverhaul.client.ClientState;
import com.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import com.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import com.cosmicdan.sleepingoverhaul.server.ServerConfig;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.BooleanSupplier;

public class SleepingOverhaul {
    public static final String MOD_ID = "sleepingoverhaul";
    public static final ResourceLocation PACKET_REALLY_SLEEPING = new ResourceLocation(MOD_ID, "is_really_sleeping");
    public static final ResourceLocation PACKET_SLEEPERROR_TIME = new ResourceLocation(MOD_ID, "sleep_error_time");
    public static final ResourceLocation PACKET_TIMELAPSE_CHANGE = new ResourceLocation(MOD_ID, "timelapse_change");

    public static IClientState CLIENT_STATE;

    private static ForgeConfigSpec CONFIG_SPEC_SERVER;
    public static ServerConfig CONFIG_SERVER;
    private static ForgeConfigSpec CONFIG_SPEC_COMMON;
    public static CommonConfig CONFIG_COMMON;

    public static long timelapseEnd = -1;

    public static final BooleanSupplier ALWAYS_TRUE_SUPPLIER = SleepingOverhaul::alwaysTrue;

    /*
    // We can use this if we don't want to use DeferredRegister
    public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));
    // Registering a new creative tab
    public static final CreativeModeTab EXAMPLE_TAB = CreativeTabRegistry.create(new ResourceLocation(MOD_ID, "example_tab"), () ->
            new ItemStack(SleepingOverhaul.EXAMPLE_ITEM.get()));
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () ->
            new Item(new Item.Properties().tab(SleepingOverhaul.EXAMPLE_TAB)));
    
    public static void init() {
        ITEMS.register();
        
        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }

     */

    public static void init() {
        // Register server/world config
        final Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        CONFIG_SPEC_SERVER = specPairServer.getRight();
        CONFIG_SERVER = specPairServer.getLeft();
        ModConfigHelper.registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC_SERVER);
        // Common config
        /*
        final Pair<CommonConfig, ForgeConfigSpec> specPairCommon = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        CONFIG_SPEC_COMMON = specPairCommon.getRight();
        CONFIG_COMMON = specPairCommon.getLeft();
        ModConfigHelper.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC_COMMON);
         */

        NetworkManager.registerReceiver(Side.C2S, PACKET_REALLY_SLEEPING, SleepingOverhaul::onReallySleepingRecv);
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(Side.S2C, PACKET_SLEEPERROR_TIME, SleepingOverhaul::onSleepErrorTimeRecv);
            NetworkManager.registerReceiver(Side.S2C, PACKET_TIMELAPSE_CHANGE, SleepingOverhaul::onTimelapseChange);
            CLIENT_STATE = new ClientState();
        } else
            CLIENT_STATE = new ClientStateDummy();
    }

    public static boolean canSleepNow(final Level level) {
        return !level.isDay();
    }

    // Received on server
    private static void onReallySleepingRecv(FriendlyByteBuf buf, PacketContext context) {
        final Player player = context.getPlayer();
        if (canSleepNow(player.level)) {
            final boolean reallySleeping = buf.readBoolean();
            //noinspection CastToIncompatibleInterface
            ((PlayerMixinProxy) player).setReallySleeping(reallySleeping);
        } else {
            final FriendlyByteBuf bufPong = new FriendlyByteBuf(Unpooled.buffer());
            bufPong.writeBoolean(true);
            NetworkManager.sendToPlayer((ServerPlayer) player, PACKET_SLEEPERROR_TIME, bufPong);
        }
    }

    // Received on client
    private static void onSleepErrorTimeRecv(FriendlyByteBuf buf, PacketContext context) {
        context.getPlayer().displayClientMessage(Player.BedSleepingProblem.NOT_POSSIBLE_NOW.getMessage(), true);
        CLIENT_STATE.doSleepButtonCooldown();
    }

    // Received on client
    private static void onTimelapseChange(FriendlyByteBuf buf, PacketContext context) {
        final Player player = context.getPlayer();
        final boolean timelapseEnabled = buf.readBoolean();
        CLIENT_STATE.setTimelapseEnabled(timelapseEnabled);
    }

    private static boolean alwaysTrue() {
        return true;
    }
}

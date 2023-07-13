package com.cosmicdan.sleepingoverhaul.mixin;

import dev.architectury.platform.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class MixinPluginCommon implements IMixinConfigPlugin {
    private static final String[] MIXINS_COMMON = {
            "MinecraftServerMixin",
            "ServerLevelMixin",
            "PlayerMixin",
            "ServerPlayerMixin",
            "ServerGamePacketLstnrMixin",
            "LivingEntityMixin",
            "NaturalSpawnerMixin",
    };
    private static final String[] MIXINS_CLIENT_ONLY = {
            "InBedChatScreenMixin",
            "CameraMixin",
    };
    private static final String[] MIXINS_SERVER_ONLY = {

    };

    @Override
    public void onLoad(String mixinPackage) {
        //InjectionInfo.register(ModifyConstantCheckedInjectionInfo.class);  // @ModifyConstantChecked
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        final List<String> mixinsToLoad = new ArrayList<>(8);
        mixinsToLoad.addAll(Arrays.asList(MIXINS_COMMON));
        switch (Platform.getEnvironment()) {
            case CLIENT -> mixinsToLoad.addAll(Arrays.asList(MIXINS_CLIENT_ONLY));
            case SERVER -> mixinsToLoad.addAll(Arrays.asList(MIXINS_SERVER_ONLY));
        }
        return mixinsToLoad;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}

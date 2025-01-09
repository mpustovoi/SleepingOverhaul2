package github.cosmicdan.sleepingoverhaul.mixin.proxy;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface PlayerMixinProxy {
    void so2_$setReallySleeping(boolean isReallySleeping);

    boolean so2_$isReallySleeping();
}

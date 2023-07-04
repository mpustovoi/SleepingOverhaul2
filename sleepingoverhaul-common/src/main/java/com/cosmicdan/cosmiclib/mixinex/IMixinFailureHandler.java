package com.cosmicdan.cosmiclib.mixinex;

import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IMixinFailureHandler {
    void onInjectionError(InjectionError injectionError);

    void onInjectionInvalid(InvalidInjectionException injectionException);
}

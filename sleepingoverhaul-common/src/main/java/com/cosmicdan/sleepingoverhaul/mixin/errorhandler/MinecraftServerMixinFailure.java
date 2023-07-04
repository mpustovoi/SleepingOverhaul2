package com.cosmicdan.sleepingoverhaul.mixin.errorhandler;

import com.cosmicdan.cosmiclib.mixinex.IMixinFailureHandler;
import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class MinecraftServerMixinFailure {
    public static class GetMsPerTickFailure implements IMixinFailureHandler {
        @Override
        public final void onInjectionError(final InjectionError injectionError) {
            // TODO: Don't throw, instead show an error screen, then when pressing Quit do the throw
            System.out.println("~~~~~~~~~~~ ERROR CAUGHT! [InjectionError]");
            throw injectionError;
        }

        @Override
        public void onInjectionInvalid(InvalidInjectionException injectionException) {
            // TODO: Don't throw, instead show an error screen, then when pressing Quit do the throw
            //       Double-check when this is used. Might only be a dev error....?
            System.out.println("~~~~~~~~~~~ ERROR CAUGHT! [InvalidInjectionException]");
            throw injectionException;
        }
    }
}

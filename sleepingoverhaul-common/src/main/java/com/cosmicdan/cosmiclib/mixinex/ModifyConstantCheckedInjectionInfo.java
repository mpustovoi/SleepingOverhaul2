package com.cosmicdan.cosmiclib.mixinex;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.ModifyConstantInjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
@AnnotationType(ModifyConstantChecked.class)
@HandlerPrefix("constant")
public class ModifyConstantCheckedInjectionInfo extends ModifyConstantInjectionInfo {
    private static final IMixinFailureHandler DEFAULT_HANDLER = new DefaultMixinFailureHandler();

    public ModifyConstantCheckedInjectionInfo(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        super(mixin, method, annotation);
    }

    @Override
    protected Injector parseInjector(final AnnotationNode injectAnnotation) {
        return new ModifyConstantCheckedInjector(this);
    }

    @Override
    protected String getDescription() {
        return "Constant modifier method (Checked version by CosmicDan)";
    }

    @SuppressWarnings("ErrorNotRethrown")
    @Override
    public void postInject() {
        try {
            super.postInject();
        } catch (final InvalidInjectionException injectionException) {
            getFailureHandler(injectionException).onInjectionInvalid(injectionException);
        } catch (final InjectionError injectionError) {
            getFailureHandler(injectionError).onInjectionError(injectionError);
        }

    }

    private IMixinFailureHandler getFailureHandler(final Throwable mixinThrowable) {
        IMixinFailureHandler failureHandler = DEFAULT_HANDLER;
        final Type failureHandlerType = Annotations.getValue(annotation, "onFailure");
        if (failureHandlerType != null) {
            try {
                final Class<?> failureHandlerClass = Class.forName(failureHandlerType.getInternalName().replace('/', '.'));
                final Object failureHandlerRaw = failureHandlerClass.getDeclaredConstructor((Class<?>[]) null).newInstance();
                if (failureHandlerRaw instanceof IMixinFailureHandler) {
                    failureHandler = (IMixinFailureHandler) failureHandlerRaw;
                } else {
                    throwMixinFailureHandlerError(new ClassCastException("onFailure class must extend " + IMixinFailureHandler.class.getCanonicalName()), mixinThrowable);
                }
            } catch (final ClassNotFoundException | InvocationTargetException | InstantiationException |
                           IllegalAccessException | NoSuchMethodException exception) {
                throwMixinFailureHandlerError(exception, mixinThrowable);
            }
        }
        return failureHandler;
    }

    private static void throwMixinFailureHandlerError(final Throwable exception, final Throwable mixinError) {
        final RuntimeException exceptionCombined = new RuntimeException(
                "Mixin error occurred. Additionally, your Mixin has an invalid onFailure entry.", exception
        );
        exceptionCombined.initCause(mixinError);
        throw exceptionCombined;
    }


    private static class DefaultMixinFailureHandler implements IMixinFailureHandler {
        private static final String msg = "A Checked Mixin failure occured, but you have not yet handled onFailure...?";

        private static void rethrowWithMessage(final Throwable throwable) {
            throw new RuntimeException(msg, throwable);
        }

        @Override
        public final void onInjectionError(final InjectionError injectionError) {
            rethrowWithMessage(injectionError);
        }

        @Override
        public final void onInjectionInvalid(final InvalidInjectionException injectionException) {
            rethrowWithMessage(injectionException);
        }
    }
}

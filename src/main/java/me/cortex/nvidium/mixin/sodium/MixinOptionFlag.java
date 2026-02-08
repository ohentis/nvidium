package me.cortex.nvidium.mixin.sodium;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.cortex.nvidium.sodiumCompat.NvidiumOptionFlags;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;

@Mixin(value = OptionFlag.class, remap = false)
public class MixinOptionFlag {

    @Shadow
    @Final
    @Mutable
    private static OptionFlag[] $VALUES = ArrayUtils
        .addAll(MixinOptionFlag.$VALUES, NvidiumOptionFlags.REQUIRES_SHADER_RELOAD);

    public MixinOptionFlag() {}

    @Invoker("<init>")
    public static OptionFlag nvidium$optionFlagCreator(String internalName, int internalId) {
        throw new AssertionError();
    }

    static {
        NvidiumOptionFlags.REQUIRES_SHADER_RELOAD = nvidium$optionFlagCreator("REQUIRES_SHADER_RELOAD", $VALUES.length);
    }
}

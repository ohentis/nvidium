package me.cortex.nvidium.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = net.minecraft.client.Minecraft.class)
public class MixinMinecraft {

    // @Inject(method = "checkGLError", at = @At("HEAD"), cancellable = true)
    // public void checkGLError(String message, CallbackInfo ci) {
    // ci.cancel();
    // }
}

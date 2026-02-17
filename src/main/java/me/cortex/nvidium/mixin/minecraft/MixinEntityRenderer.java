package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.cortex.nvidium.Nvidium;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private float farPlaneDistance;

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
    private float changeFog(float a, float b) {
        if (Nvidium.IS_ENABLED) {
            return a;
        } else {
            return Math.min(a, b);
        }
    }

    @Inject(
        method = "setupCameraTransform",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void changeFarPlaneDistance(float p_78479_1_, int p_78479_2_, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            farPlaneDistance = 16 * 512f;
        }
    }
}

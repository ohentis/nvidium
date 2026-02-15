package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import me.cortex.nvidium.Nvidium;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private float farPlaneDistance;

    @ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 192.0F))
    private float changeFog(float fog) {
        if (Nvidium.IS_ENABLED) {
            return 9999999f;
        } else {
            return fog;
        }
    }

    @Inject(
        method = "setupCameraTransform",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void changeFarPlaneDistance(float p_78479_1_, int p_78479_2_, CallbackInfo ci){
        if(Nvidium.IS_ENABLED) {
            farPlaneDistance = 16 * 512f;
        }
    }
}
